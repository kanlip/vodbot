package com.example.demo.platform.adapter.in.web;

import com.example.demo.platform.domain.PlatformIntegration;
import com.example.demo.platform.port.in.*;
import com.example.demo.shared.domain.Platform;
import com.example.demo.users.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/platform/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Platform Authorization", description = "OAuth authorization with e-commerce platforms")
public class PlatformAuthController {

    private final PlatformAuthorizationUseCase authorizationUseCase;
    private final ProductSyncUseCase productSyncUseCase;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate OAuth flow for platform integration")
    public ResponseEntity<AuthUrlResponse> initiateOAuth(@RequestBody InitiateAuthRequest request) {
        User authenticatedUser = getCurrentUser();
        log.info("Initiating OAuth for platform {} in org {} by user {}",
                request.getPlatform(), authenticatedUser.getOrgId(), authenticatedUser.getEmail());

        InitiateOAuthCommand command = InitiateOAuthCommand.builder()
                .orgId(authenticatedUser.getOrgId())
                .platform(request.getPlatform())
                .sellerId(request.getSellerId()) // For Lazada, this will be null and populated from OAuth response
                .redirectUrl(request.getRedirectUrl())
                .webhookSecret(request.getWebhookSecret())
                .build();

        String authUrl = authorizationUseCase.initiateOAuthFlow(command);

        return ResponseEntity.ok(AuthUrlResponse.builder()
                .authUrl(authUrl)
                .platform(request.getPlatform())
                .build());
    }

    @PostMapping("/callback")
    @Operation(summary = "Handle OAuth callback from platform")
    public ResponseEntity<?> handleOAuthCallback(@RequestBody OAuthCallbackRequest request) {
        log.info("Processing OAuth callback for state: {}", request.getState());

        try {
            CompleteOAuthCommand command = CompleteOAuthCommand.builder()
                    .state(request.getState())
                    .authorizationCode(request.getCode())
                    .shopId(request.getShopId())
                    .build();

            PlatformIntegration integration = authorizationUseCase.completeOAuthFlow(command);

            // Start initial product sync asynchronously
            // productSyncUseCase.syncAllProducts(integration.getId()); // Would run in background

            return ResponseEntity.ok(PlatformIntegrationResponse.from(integration));

        } catch (Exception e) {
            log.error("OAuth callback processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ErrorResponse.builder()
                    .error("oauth_failed")
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/integrations")
    @Operation(summary = "Get all platform integrations for current user's organization")
    public ResponseEntity<List<PlatformIntegrationResponse>> getIntegrations() {
        User authenticatedUser = getCurrentUser();
        List<PlatformIntegration> integrations = authorizationUseCase.getOrganizationIntegrations(authenticatedUser.getOrgId());

        List<PlatformIntegrationResponse> response = integrations.stream()
                .map(PlatformIntegrationResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/integrations/{integrationId}")
    @Operation(summary = "Get platform integration details")
    public ResponseEntity<PlatformIntegrationResponse> getIntegration(@PathVariable UUID integrationId) {
        User authenticatedUser = getCurrentUser();
        PlatformIntegration integration = authorizationUseCase.getIntegration(integrationId);

        // Ensure user can only access integrations from their organization
        if (!integration.getOrgId().equals(authenticatedUser.getOrgId())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(PlatformIntegrationResponse.from(integration));
    }

    @PostMapping("/integrations/{integrationId}/refresh")
    @Operation(summary = "Refresh access token for integration")
    public ResponseEntity<PlatformIntegrationResponse> refreshToken(@PathVariable UUID integrationId) {
        User authenticatedUser = getCurrentUser();
        PlatformIntegration integration = authorizationUseCase.getIntegration(integrationId);

        // Ensure user can only refresh tokens for their organization's integrations
        if (!integration.getOrgId().equals(authenticatedUser.getOrgId())) {
            return ResponseEntity.notFound().build();
        }

        integration = authorizationUseCase.refreshAccessToken(integrationId);
        return ResponseEntity.ok(PlatformIntegrationResponse.from(integration));
    }

    @DeleteMapping("/integrations/{integrationId}")
    @Operation(summary = "Revoke platform integration")
    public ResponseEntity<Void> revokeIntegration(@PathVariable UUID integrationId) {
        User authenticatedUser = getCurrentUser();
        PlatformIntegration integration = authorizationUseCase.getIntegration(integrationId);

        // Ensure user can only revoke integrations from their organization
        if (!integration.getOrgId().equals(authenticatedUser.getOrgId())) {
            return ResponseEntity.notFound().build();
        }

        authorizationUseCase.revokeIntegration(integrationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/integrations/{integrationId}/sync")
    @Operation(summary = "Trigger product sync for integration")
    public ResponseEntity<Void> syncProducts(@PathVariable UUID integrationId) {
        User authenticatedUser = getCurrentUser();
        PlatformIntegration integration = authorizationUseCase.getIntegration(integrationId);

        // Ensure user can only sync integrations from their organization
        if (!integration.getOrgId().equals(authenticatedUser.getOrgId())) {
            return ResponseEntity.notFound().build();
        }

        log.info("Triggering product sync for integration: {} by user: {}", integrationId, authenticatedUser.getEmail());
        // This would typically be run asynchronously
        // productSyncUseCase.syncAllProducts(integrationId);
        return ResponseEntity.accepted().build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }
}