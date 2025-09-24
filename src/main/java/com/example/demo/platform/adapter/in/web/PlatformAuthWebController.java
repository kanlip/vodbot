package com.example.demo.platform.adapter.in.web;

import com.example.demo.platform.configuration.PlatformOAuthProperties;
import com.example.demo.platform.domain.PlatformIntegration;
import com.example.demo.platform.port.in.*;
import com.example.demo.shared.domain.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/platform")
@RequiredArgsConstructor
@Slf4j
public class PlatformAuthWebController {

    private final PlatformAuthorizationUseCase authorizationUseCase;
    private final ProductSyncUseCase productSyncUseCase;
    private final PlatformOAuthProperties oauthProperties;

    @GetMapping("/integrations")
    public String showIntegrations(@RequestParam(required = false) UUID orgId, Model model) {
        if (orgId == null) {
            // For demo purposes, using a default org ID - in real app, get from security context
            orgId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        }

        List<PlatformIntegration> integrations = authorizationUseCase.getOrganizationIntegrations(orgId);

        model.addAttribute("integrations", integrations);
        model.addAttribute("orgId", orgId);
        model.addAttribute("platforms", Platform.values());

        return "platform/integrations";
    }

    @GetMapping("/authorize")
    public String showAuthorizePage(@RequestParam Platform platform,
                                   @RequestParam(required = false) UUID orgId,
                                   Model model) {
        if (orgId == null) {
            orgId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        }

        model.addAttribute("platform", platform);
        model.addAttribute("orgId", orgId);
        model.addAttribute("platformName", getPlatformDisplayName(platform));
        model.addAttribute("platformDescription", getPlatformDescription(platform));
        model.addAttribute("platformIcon", getPlatformIcon(platform));

        return "platform/authorize";
    }

    @PostMapping("/authorize")
    public String initiateAuthorization(@RequestParam Platform platform,
                                      @RequestParam UUID orgId,
                                      @RequestParam String sellerId,
                                      @RequestParam(required = false) String webhookSecret,
                                      RedirectAttributes redirectAttributes) {
        try {
            String redirectUrl = getCallbackUrl();

            InitiateOAuthCommand command = InitiateOAuthCommand.builder()
                    .orgId(orgId)
                    .platform(platform)
                    .sellerId(sellerId)
                    .redirectUrl(redirectUrl)
                    .webhookSecret(webhookSecret != null ? webhookSecret : "default-webhook-secret")
                    .build();

            String authUrl = authorizationUseCase.initiateOAuthFlow(command);

            log.info("Redirecting to {} authorization URL for org {}", platform, orgId);
            return "redirect:" + authUrl;

        } catch (Exception e) {
            log.error("Failed to initiate authorization for {} in org {}: {}", platform, orgId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to start authorization: " + e.getMessage());
            return "redirect:/platform/integrations?orgId=" + orgId;
        }
    }

    @GetMapping("/callback")
    public String handleOAuthCallback(@RequestParam String state,
                                    @RequestParam(required = false) String code,
                                    @RequestParam(required = false) String shop_id,
                                    @RequestParam(required = false) String error,
                                    RedirectAttributes redirectAttributes) {
        if (error != null) {
            log.error("OAuth callback received error: {}", error);
            redirectAttributes.addFlashAttribute("error", "Authorization failed: " + error);
            return "redirect:/platform/integrations";
        }

        if (code == null) {
            log.error("OAuth callback missing authorization code");
            redirectAttributes.addFlashAttribute("error", "Authorization failed: Missing authorization code");
            return "redirect:/platform/integrations";
        }

        try {
            CompleteOAuthCommand command = CompleteOAuthCommand.builder()
                    .state(state)
                    .authorizationCode(code)
                    .shopId(shop_id)
                    .build();

            PlatformIntegration integration = authorizationUseCase.completeOAuthFlow(command);

            log.info("Successfully authorized {} integration: {}", integration.getPlatform(), integration.getId());
            redirectAttributes.addFlashAttribute("success",
                "Successfully connected to " + getPlatformDisplayName(integration.getPlatform()) + "!");

            return "redirect:/platform/integrations?orgId=" + integration.getOrgId();

        } catch (IllegalArgumentException e) {
            log.error("Invalid OAuth flow for state {}: {}", state, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Invalid authorization request. Please try again.");
            return "redirect:/platform/integrations";
        } catch (IllegalStateException e) {
            log.error("OAuth flow state error for state {}: {}", state, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Authorization already completed or expired. Please try again.");
            return "redirect:/platform/integrations";
        } catch (Exception e) {
            log.error("Failed to complete OAuth flow for state {}: {}", state, e.getMessage(), e);

            // Provide user-friendly error messages based on the error
            String userMessage = getUserFriendlyErrorMessage(e.getMessage());
            redirectAttributes.addFlashAttribute("error", userMessage);
            return "redirect:/platform/integrations";
        }
    }

    @PostMapping("/integrations/{integrationId}/refresh")
    public String refreshToken(@PathVariable UUID integrationId, RedirectAttributes redirectAttributes) {
        try {
            PlatformIntegration integration = authorizationUseCase.refreshAccessToken(integrationId);
            redirectAttributes.addFlashAttribute("success",
                "Successfully refreshed " + getPlatformDisplayName(integration.getPlatform()) + " token");
            return "redirect:/platform/integrations?orgId=" + integration.getOrgId();
        } catch (Exception e) {
            log.error("Failed to refresh token for integration {}: {}", integrationId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to refresh token: " + e.getMessage());
            return "redirect:/platform/integrations";
        }
    }

    @PostMapping("/integrations/{integrationId}/revoke")
    public String revokeIntegration(@PathVariable UUID integrationId, RedirectAttributes redirectAttributes) {
        try {
            PlatformIntegration integration = authorizationUseCase.getIntegration(integrationId);
            authorizationUseCase.revokeIntegration(integrationId);
            redirectAttributes.addFlashAttribute("success",
                "Successfully disconnected " + getPlatformDisplayName(integration.getPlatform()));
            return "redirect:/platform/integrations?orgId=" + integration.getOrgId();
        } catch (Exception e) {
            log.error("Failed to revoke integration {}: {}", integrationId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to disconnect integration: " + e.getMessage());
            return "redirect:/platform/integrations";
        }
    }

    @PostMapping("/integrations/{integrationId}/sync")
    public String syncProducts(@PathVariable UUID integrationId, RedirectAttributes redirectAttributes) {
        try {
            PlatformIntegration integration = authorizationUseCase.getIntegration(integrationId);
            // Note: In a real implementation, this would trigger async product sync
            log.info("Product sync triggered for integration: {}", integrationId);
            redirectAttributes.addFlashAttribute("success",
                "Product sync started for " + getPlatformDisplayName(integration.getPlatform()));
            return "redirect:/platform/integrations?orgId=" + integration.getOrgId();
        } catch (Exception e) {
            log.error("Failed to trigger sync for integration {}: {}", integrationId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to start product sync: " + e.getMessage());
            return "redirect:/platform/integrations";
        }
    }

    private String getCallbackUrl() {
        return oauthProperties.getOauth().getCallbackUrl();
    }

    private String getPlatformDisplayName(Platform platform) {
        return switch (platform) {
            case SHOPEE -> "Shopee";
            case LAZADA -> "Lazada";
            case TIKTOK -> "TikTok Shop";
        };
    }

    private String getPlatformDescription(Platform platform) {
        return switch (platform) {
            case SHOPEE -> "Connect your Shopee store to sync products and manage orders";
            case LAZADA -> "Connect your Lazada store to sync products and manage orders";
            case TIKTOK -> "Connect your TikTok Shop to sync products and manage orders";
        };
    }

    private String getPlatformIcon(Platform platform) {
        return switch (platform) {
            case SHOPEE -> "fab fa-shopify"; // Using similar icon as placeholder
            case LAZADA -> "fas fa-store";
            case TIKTOK -> "fab fa-tiktok";
        };
    }

    private String getUserFriendlyErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return "An unexpected error occurred. Please try again.";
        }

        String lowerError = errorMessage.toLowerCase();

        if (lowerError.contains("invalid_grant") || lowerError.contains("authorization_pending")) {
            return "Authorization code has expired or is invalid. Please try connecting again.";
        } else if (lowerError.contains("access_denied")) {
            return "Access was denied. Please make sure you approve the authorization request.";
        } else if (lowerError.contains("network") || lowerError.contains("connection")) {
            return "Network error occurred. Please check your internet connection and try again.";
        } else if (lowerError.contains("authentication") || lowerError.contains("unauthorized")) {
            return "Authentication failed. Please verify your platform credentials and try again.";
        } else if (lowerError.contains("rate limit") || lowerError.contains("throttled")) {
            return "Too many requests. Please wait a moment and try again.";
        } else if (lowerError.contains("invalid_client")) {
            return "Invalid application credentials. Please contact support.";
        } else {
            return "Failed to complete authorization. Please try again or contact support if the problem persists.";
        }
    }
}