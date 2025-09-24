package com.example.demo.platform.application;

import com.example.demo.platform.configuration.PlatformOAuthProperties;
import com.example.demo.platform.domain.PlatformIntegration;
import com.example.demo.platform.port.in.CompleteOAuthCommand;
import com.example.demo.platform.port.in.InitiateOAuthCommand;
import com.example.demo.platform.port.in.PlatformAuthorizationUseCase;
import com.example.demo.platform.port.out.PlatformIntegrationRepository;
import com.example.demo.shared.domain.Platform;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlatformAuthorizationService implements PlatformAuthorizationUseCase {

    private final PlatformIntegrationRepository integrationRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PlatformOAuthProperties oauthProperties;

    // OAuth URLs now come from configuration

    @Override
    public String initiateOAuthFlow(InitiateOAuthCommand command) {
        log.info("Initiating OAuth flow for platform {} in org {}", command.getPlatform(), command.getOrgId());

        // Generate unique state for OAuth flow
        String state = UUID.randomUUID().toString();

        // Create integration record
        PlatformIntegration integration = PlatformIntegration.builder()
                .id(UUID.randomUUID())
                .orgId(command.getOrgId())
                .platform(command.getPlatform())
                .sellerId(command.getSellerId()) // For Lazada, this will be null and set later from OAuth response
                .webhookSecret(command.getWebhookSecret())
                .oauthState(state)
                .active(true)
                .authorizationStatus(PlatformIntegration.AuthorizationStatus.PENDING)
                .syncStatus(PlatformIntegration.SyncStatus.NEVER_SYNCED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        integrationRepository.save(integration);

        // Generate platform-specific authorization URL
        return generateAuthorizationUrl(command.getPlatform(), state, command.getRedirectUrl());
    }

    @Override
    public PlatformIntegration completeOAuthFlow(CompleteOAuthCommand command) {
        log.info("Completing OAuth flow for state: {}", command.getState());

        // Find integration by OAuth state
        PlatformIntegration integration = integrationRepository.findByOAuthState(command.getState())
                .orElseThrow(() -> new IllegalArgumentException("Invalid OAuth state: " + command.getState()));

        if (integration.getAuthorizationStatus() != PlatformIntegration.AuthorizationStatus.PENDING) {
            throw new IllegalStateException("OAuth flow already completed for state: " + command.getState());
        }

        // Exchange authorization code for access token
        TokenResponse tokenResponse = exchangeCodeForToken(integration.getPlatform(), command.getAuthorizationCode(), integration);

        // Update integration with tokens
        integration.authorize(
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresAt(),
                tokenResponse.getSellerId() != null ? tokenResponse.getSellerId() : (command.getShopId() != null ? command.getShopId() : integration.getSellerId()),
                tokenResponse.getShopName()
        );

        // Store API credentials
        Map<String, Object> credentials = createCredentialsMap(integration.getPlatform(), tokenResponse);
        try {
            integration.setApiCredentials(objectMapper.writeValueAsString(credentials));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize API credentials", e);
        }

        return integrationRepository.save(integration);
    }

    @Override
    public PlatformIntegration refreshAccessToken(UUID integrationId) {
        log.info("Refreshing access token for integration: {}", integrationId);

        PlatformIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found: " + integrationId));

        if (!integration.needsTokenRefresh()) {
            log.info("Token refresh not needed for integration: {}", integrationId);
            return integration;
        }

        if (integration.getRefreshToken() == null) {
            throw new IllegalStateException("No refresh token available for integration: " + integrationId);
        }

        try {
            TokenResponse tokenResponse = refreshAccessToken(integration.getPlatform(), integration.getRefreshToken(), integration);

            integration.refreshToken(tokenResponse.getAccessToken(), tokenResponse.getExpiresAt());

            // Update credentials if needed
            Map<String, Object> credentials = createCredentialsMap(integration.getPlatform(), tokenResponse);
            integration.setApiCredentials(objectMapper.writeValueAsString(credentials));

            return integrationRepository.save(integration);

        } catch (Exception e) {
            log.error("Failed to refresh token for integration {}: {}", integrationId, e.getMessage());
            integration.setAuthorizationStatus(PlatformIntegration.AuthorizationStatus.EXPIRED);
            integrationRepository.save(integration);
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }

    @Override
    public void revokeIntegration(UUID integrationId) {
        log.info("Revoking integration: {}", integrationId);

        PlatformIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found: " + integrationId));

        integration.revoke();
        integration.setActive(false);

        integrationRepository.save(integration);

        log.info("Integration revoked: {}", integrationId);
    }

    @Override
    @Transactional(readOnly = true)
    public PlatformIntegration getIntegration(UUID integrationId) {
        return integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found: " + integrationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatformIntegration> getOrganizationIntegrations(UUID orgId) {
        return integrationRepository.findByOrgId(orgId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatformIntegration> getAuthorizedIntegrations(UUID orgId) {
        return integrationRepository.findAuthorizedByOrgId(orgId);
    }

    private String generateAuthorizationUrl(Platform platform, String state, String redirectUrl) {
        return switch (platform) {
            case SHOPEE -> generateShopeeAuthUrl(state, redirectUrl);
            case LAZADA -> generateLazadaAuthUrl(state, redirectUrl);
            case TIKTOK -> generateTikTokAuthUrl(state, redirectUrl);
        };
    }

    private String generateShopeeAuthUrl(String state, String redirectUrl) {
        String partnerId = oauthProperties.getShopee().getApp().getPartnerId();
        return UriComponentsBuilder.fromUriString(oauthProperties.getShopee().getOauth().getAuthUrl())
                .queryParam("partner_id", partnerId)
                .queryParam("redirect", redirectUrl)
                .queryParam("state", state)
                .build().toUriString();
    }

    private String generateLazadaAuthUrl(String state, String redirectUrl) {
        String appKey = oauthProperties.getLazada().getApp().getKey();

        // Build Lazada OAuth URL according to their API documentation
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(oauthProperties.getLazada().getOauth().getAuthUrl())
                .queryParam("response_type", "code")
                .queryParam("force_auth", "true")
                .queryParam("client_id", appKey)
                .queryParam("redirect_uri", redirectUrl)
                .queryParam("state", state);

        // Add country-specific parameter if needed (some regions require this)
        // You can configure this based on your target market
        // builder.queryParam("country", "TH"); // For Thailand market

        String authUrl = builder.build().toUriString();
        log.debug("Generated Lazada auth URL: {}", authUrl.replaceAll("client_id=[^&]*", "client_id=***"));

        return authUrl;
    }

    private String generateTikTokAuthUrl(String state, String redirectUrl) {
        String appKey = oauthProperties.getTiktok().getApp().getKey();
        return UriComponentsBuilder.fromUriString(oauthProperties.getTiktok().getOauth().getAuthUrl())
                .queryParam("app_key", appKey)
                .queryParam("state", state)
                .queryParam("redirect_uri", redirectUrl)
                .build().toUriString();
    }

    private TokenResponse exchangeCodeForToken(Platform platform, String authCode, PlatformIntegration integration) {
        return switch (platform) {
            case SHOPEE -> exchangeShopeeToken(authCode, integration);
            case LAZADA -> exchangeLazadaToken(authCode, integration);
            case TIKTOK -> exchangeTikTokToken(authCode, integration);
        };
    }

    private TokenResponse exchangeShopeeToken(String authCode, PlatformIntegration integration) {
        // Implement Shopee token exchange
        // This is a simplified example - actual implementation would need proper API calls
        return TokenResponse.builder()
                .accessToken("shopee_access_token_" + UUID.randomUUID())
                .refreshToken("shopee_refresh_token_" + UUID.randomUUID())
                .expiresAt(Instant.now().plusSeconds(3600))
                .shopName("Shopee Shop")
                .build();
    }

    private TokenResponse exchangeLazadaToken(String authCode, PlatformIntegration integration) {
        try {
            log.info("Exchanging Lazada authorization code for access token, integration: {}", integration.getId());

            String appKey = oauthProperties.getLazada().getApp().getKey();
            String appSecret = oauthProperties.getLazada().getApp().getSecret();
            String tokenUrl = oauthProperties.getLazada().getOauth().getTokenUrl();
            String callbackUrl = oauthProperties.getOauth().getCallbackUrl();

            // Prepare request parameters
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", appKey);
            params.add("client_secret", appSecret);
            params.add("code", authCode);
            params.add("redirect_uri", callbackUrl);

            // Create signature for Lazada API
            String timestamp = String.valueOf(System.currentTimeMillis());
            String signature = generateLazadaSignature("/auth/token/create", params, appSecret, timestamp);

            // Add signature parameters
            params.add("sign_method", "sha256");
            params.add("timestamp", timestamp);
            params.add("sign", signature);

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("User-Agent", "VodBot/1.0");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // Make the API call
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Lazada token exchange failed with status: " + response.getStatusCode());
            }

            // Parse response
            JsonNode responseNode = objectMapper.readTree(response.getBody());

            if (responseNode.has("error")) {
                String error = responseNode.get("error").asText();
                String errorDescription = responseNode.has("error_description")
                    ? responseNode.get("error_description").asText()
                    : "Unknown error";
                throw new RuntimeException("Lazada API error: " + error + " - " + errorDescription);
            }

            String accessToken = responseNode.get("access_token").asText();
            String refreshToken = responseNode.has("refresh_token") ? responseNode.get("refresh_token").asText() : null;
            long expiresIn = responseNode.has("expires_in") ? responseNode.get("expires_in").asLong() : 3600;

            // Get seller/shop information if available
            String shopName = responseNode.has("account") ? responseNode.get("account").asText() : "Lazada Shop";

            // Extract seller_id for Thailand from country_user_info
            String sellerId = null;
            if (responseNode.has("country_user_info")) {
                JsonNode countryUserInfo = responseNode.get("country_user_info");
                if (countryUserInfo.isArray()) {
                    for (JsonNode countryInfo : countryUserInfo) {
                        if (countryInfo.has("country") && "th".equals(countryInfo.get("country").asText())) {
                            if (countryInfo.has("seller_id")) {
                                sellerId = countryInfo.get("seller_id").asText();
                                break;
                            }
                        }
                    }
                }
            }

            if (sellerId == null) {
                throw new RuntimeException("No seller_id found for Thailand (th) in Lazada response");
            }

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresAt(Instant.now().plusSeconds(expiresIn))
                    .shopName(shopName)
                    .sellerId(sellerId)
                    .build();

        } catch (HttpClientErrorException e) {
            log.error("Lazada token exchange HTTP error: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("Lazada authentication failed: " + e.getMessage(), e);
        } catch (RestClientException e) {
            log.error("Lazada token exchange REST error", e);
            throw new RuntimeException("Lazada API communication error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Lazada token exchange unexpected error", e);
            throw new RuntimeException("Failed to exchange Lazada authorization code", e);
        }
    }

    private TokenResponse exchangeTikTokToken(String authCode, PlatformIntegration integration) {
        // Implement TikTok token exchange
        return TokenResponse.builder()
                .accessToken("tiktok_access_token_" + UUID.randomUUID())
                .refreshToken("tiktok_refresh_token_" + UUID.randomUUID())
                .expiresAt(Instant.now().plusSeconds(3600))
                .shopName("TikTok Shop")
                .build();
    }

    private TokenResponse refreshAccessToken(Platform platform, String refreshToken, PlatformIntegration integration) {
        return switch (platform) {
            case SHOPEE -> refreshShopeeToken(refreshToken, integration);
            case LAZADA -> refreshLazadaToken(refreshToken, integration);
            case TIKTOK -> refreshTikTokToken(refreshToken, integration);
        };
    }

    private TokenResponse refreshShopeeToken(String refreshToken, PlatformIntegration integration) {
        // Implement Shopee token refresh
        return TokenResponse.builder()
                .accessToken("shopee_new_access_token_" + UUID.randomUUID())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private TokenResponse refreshLazadaToken(String refreshToken, PlatformIntegration integration) {
        try {
            log.info("Refreshing Lazada access token for integration: {}", integration.getId());

            String appKey = oauthProperties.getLazada().getApp().getKey();
            String appSecret = oauthProperties.getLazada().getApp().getSecret();
            String refreshUrl = oauthProperties.getLazada().getOauth().getRefreshUrl();

            // Prepare request parameters
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "refresh_token");
            params.add("client_id", appKey);
            params.add("client_secret", appSecret);
            params.add("refresh_token", refreshToken);

            // Create signature for Lazada API
            String timestamp = String.valueOf(System.currentTimeMillis());
            String signature = generateLazadaSignature("/auth/token/refresh", params, appSecret, timestamp);

            // Add signature parameters
            params.add("sign_method", "sha256");
            params.add("timestamp", timestamp);
            params.add("sign", signature);

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("User-Agent", "VodBot/1.0");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // Make the API call
            ResponseEntity<String> response = restTemplate.postForEntity(refreshUrl, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Lazada token refresh failed with status: " + response.getStatusCode());
            }

            // Parse response
            JsonNode responseNode = objectMapper.readTree(response.getBody());

            if (responseNode.has("error")) {
                String error = responseNode.get("error").asText();
                String errorDescription = responseNode.has("error_description")
                    ? responseNode.get("error_description").asText()
                    : "Unknown error";
                throw new RuntimeException("Lazada API error during refresh: " + error + " - " + errorDescription);
            }

            String accessToken = responseNode.get("access_token").asText();
            long expiresIn = responseNode.has("expires_in") ? responseNode.get("expires_in").asLong() : 3600;

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .expiresAt(Instant.now().plusSeconds(expiresIn))
                    .build();

        } catch (HttpClientErrorException e) {
            log.error("Lazada token refresh HTTP error: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("Lazada token refresh failed: " + e.getMessage(), e);
        } catch (RestClientException e) {
            log.error("Lazada token refresh REST error", e);
            throw new RuntimeException("Lazada API communication error during refresh: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Lazada token refresh unexpected error", e);
            throw new RuntimeException("Failed to refresh Lazada access token", e);
        }
    }

    private TokenResponse refreshTikTokToken(String refreshToken, PlatformIntegration integration) {
        // Implement TikTok token refresh
        return TokenResponse.builder()
                .accessToken("tiktok_new_access_token_" + UUID.randomUUID())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private Map<String, Object> createCredentialsMap(Platform platform, TokenResponse tokenResponse) {
        Map<String, Object> credentials = new HashMap<>();

        switch (platform) {
            case SHOPEE -> {
                credentials.put("partner_id", oauthProperties.getShopee().getApp().getPartnerId());
                credentials.put("app_secret", oauthProperties.getShopee().getApp().getSecret());
            }
            case LAZADA -> {
                credentials.put("app_key", oauthProperties.getLazada().getApp().getKey());
                credentials.put("app_secret", oauthProperties.getLazada().getApp().getSecret());
            }
            case TIKTOK -> {
                credentials.put("app_key", oauthProperties.getTiktok().getApp().getKey());
                credentials.put("app_secret", oauthProperties.getTiktok().getApp().getSecret());
            }
        }

        return credentials;
    }

    /**
     * Generate signature for Lazada API requests
     * According to Lazada API documentation
     */
    private String generateLazadaSignature(String apiPath, MultiValueMap<String, String> params, String appSecret, String timestamp) {
        try {
            // Sort parameters
            Map<String, String> sortedParams = new HashMap<>();
            params.forEach((key, values) -> {
                if (!values.isEmpty() && !"sign".equals(key)) {
                    sortedParams.put(key, values.get(0));
                }
            });

            // Build string to sign
            StringBuilder stringToSign = new StringBuilder();
            stringToSign.append(apiPath);

            sortedParams.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        stringToSign.append(entry.getKey()).append(entry.getValue());
                    });

            // Generate HMAC-SHA256 signature
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(stringToSign.toString().getBytes(StandardCharsets.UTF_8));

            // Convert to hex string and uppercase
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString().toUpperCase();

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate Lazada API signature", e);
        }
    }


    @lombok.Builder
    @lombok.Data
    private static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private Instant expiresAt;
        private String shopName;
        private String sellerId;
    }
}