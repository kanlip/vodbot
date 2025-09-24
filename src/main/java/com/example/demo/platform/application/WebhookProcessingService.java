package com.example.demo.platform.application;

import com.example.demo.platform.port.in.WebhookHandlerUseCase;
import com.example.demo.platform.port.in.ProductSyncUseCase;
import com.example.demo.platform.port.out.PlatformIntegrationRepository;
import com.example.demo.platform.domain.PlatformIntegration;
import com.example.demo.shared.domain.Platform;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebhookProcessingService implements WebhookHandlerUseCase {

    private final PlatformIntegrationRepository integrationRepository;
    private final ProductSyncUseCase productSyncUseCase;
    private final ObjectMapper objectMapper;

    @Override
    public void processWebhook(Platform platform, String integrationId, Map<String, Object> payload, Map<String, String> headers) {
        log.info("Processing webhook from platform {} for integration {}", platform, integrationId);

        try {
            // Find integration
            UUID integrationUUID = UUID.fromString(integrationId);
            PlatformIntegration integration = integrationRepository.findById(integrationUUID)
                    .orElseThrow(() -> new IllegalArgumentException("Integration not found: " + integrationId));

            // Validate webhook signature if secret is available
            if (integration.getWebhookSecret() != null && !integration.getWebhookSecret().isEmpty()) {
                String signature = headers.get("signature");
                if (signature == null) {
                    signature = headers.get("x-signature"); // Some platforms use different header names
                }

                if (signature != null && !validateWebhookSignature(platform, payload, signature, integration.getWebhookSecret())) {
                    log.warn("Invalid webhook signature for integration {}", integrationId);
                    return;
                }
            }

            // Process webhook based on platform and event type
            String eventType = extractEventType(platform, payload);
            log.info("Processing webhook event type: {} for platform: {}", eventType, platform);

            processWebhookEvent(platform, integration, eventType, payload);

        } catch (Exception e) {
            log.error("Error processing webhook for platform {} integration {}: {}", platform, integrationId, e.getMessage(), e);
            // Don't throw exception to avoid webhook retry loops
        }
    }

    @Override
    public void retryFailedWebhooks() {
        log.info("Retrying failed webhooks - not implemented yet");
        // TODO: Implement webhook retry logic
    }

    private String extractEventType(Platform platform, Map<String, Object> payload) {
        return switch (platform) {
            case SHOPEE -> extractShopeeEventType(payload);
            case LAZADA -> extractLazadaEventType(payload);
            case TIKTOK -> extractTikTokEventType(payload);
        };
    }

    private String extractShopeeEventType(Map<String, Object> payload) {
        return payload.getOrDefault("event", "unknown").toString();
    }

    private String extractLazadaEventType(Map<String, Object> payload) {
        return payload.getOrDefault("event_type", "unknown").toString();
    }

    private String extractTikTokEventType(Map<String, Object> payload) {
        return payload.getOrDefault("type", "unknown").toString();
    }

    private void processWebhookEvent(Platform platform, PlatformIntegration integration, String eventType, Map<String, Object> payload) {
        switch (platform) {
            case SHOPEE -> processShopeeWebhook(integration, eventType, payload);
            case LAZADA -> processLazadaWebhook(integration, eventType, payload);
            case TIKTOK -> processTikTokWebhook(integration, eventType, payload);
        }
    }

    private void processShopeeWebhook(PlatformIntegration integration, String eventType, Map<String, Object> payload) {
        log.info("Processing Shopee webhook event: {}", eventType);

        switch (eventType.toLowerCase()) {
            case "item_update", "item_add" -> {
                // Product updated or added
                String itemId = extractShopeeItemId(payload);
                if (itemId != null) {
                    log.info("Syncing Shopee product: {}", itemId);
                    productSyncUseCase.syncProductById(integration.getId(), itemId);
                }
            }
            case "order_update" -> {
                // Order status changed
                String orderSn = extractShopeeOrderSn(payload);
                log.info("Shopee order updated: {}", orderSn);
                // TODO: Update order status in system
            }
            case "order_cancel" -> {
                // Order cancelled
                String orderSn = extractShopeeOrderSn(payload);
                log.info("Shopee order cancelled: {}", orderSn);
                // TODO: Handle order cancellation
            }
            default -> log.info("Unhandled Shopee webhook event: {}", eventType);
        }
    }

    private void processLazadaWebhook(PlatformIntegration integration, String eventType, Map<String, Object> payload) {
        log.info("Processing Lazada webhook event: {}", eventType);

        switch (eventType.toLowerCase()) {
            case "product_update", "product_create" -> {
                // Product updated or created
                String itemId = extractLazadaItemId(payload);
                if (itemId != null) {
                    log.info("Syncing Lazada product: {}", itemId);
                    productSyncUseCase.syncProductById(integration.getId(), itemId);
                }
            }
            case "order_update" -> {
                // Order status changed
                String orderId = extractLazadaOrderId(payload);
                log.info("Lazada order updated: {}", orderId);
                // TODO: Update order status in system
            }
            case "order_cancel" -> {
                // Order cancelled
                String orderId = extractLazadaOrderId(payload);
                log.info("Lazada order cancelled: {}", orderId);
                // TODO: Handle order cancellation
            }
            default -> log.info("Unhandled Lazada webhook event: {}", eventType);
        }
    }

    private void processTikTokWebhook(PlatformIntegration integration, String eventType, Map<String, Object> payload) {
        log.info("Processing TikTok webhook event: {}", eventType);

        switch (eventType.toLowerCase()) {
            case "product_update", "product_create" -> {
                // Product updated or created
                String productId = extractTikTokProductId(payload);
                if (productId != null) {
                    log.info("Syncing TikTok product: {}", productId);
                    productSyncUseCase.syncProductById(integration.getId(), productId);
                }
            }
            case "order_status_update" -> {
                // Order status changed
                String orderId = extractTikTokOrderId(payload);
                log.info("TikTok order updated: {}", orderId);
                // TODO: Update order status in system
            }
            case "order_cancel" -> {
                // Order cancelled
                String orderId = extractTikTokOrderId(payload);
                log.info("TikTok order cancelled: {}", orderId);
                // TODO: Handle order cancellation
            }
            default -> log.info("Unhandled TikTok webhook event: {}", eventType);
        }
    }

    private boolean validateWebhookSignature(Platform platform, Map<String, Object> payload, String signature, String secret) {
        try {
            String payloadString = objectMapper.writeValueAsString(payload);

            return switch (platform) {
                case SHOPEE -> validateShopeeSignature(payloadString, signature, secret);
                case LAZADA -> validateLazadaSignature(payloadString, signature, secret);
                case TIKTOK -> validateTikTokSignature(payloadString, signature, secret);
            };
        } catch (Exception e) {
            log.error("Error validating webhook signature: {}", e.getMessage());
            return false;
        }
    }

    private boolean validateShopeeSignature(String payload, String signature, String secret) {
        // Implement Shopee webhook signature validation
        // This would use HMAC-SHA256 with the webhook secret
        return true; // Simplified for now
    }

    private boolean validateLazadaSignature(String payload, String signature, String secret) {
        // Implement Lazada webhook signature validation
        return true; // Simplified for now
    }

    private boolean validateTikTokSignature(String payload, String signature, String secret) {
        // Implement TikTok webhook signature validation
        return true; // Simplified for now
    }

    // Helper methods to extract IDs from webhook payloads
    private String extractShopeeItemId(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        return data != null ? String.valueOf(data.get("item_id")) : null;
    }

    private String extractShopeeOrderSn(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        return data != null ? String.valueOf(data.get("order_sn")) : null;
    }

    private String extractLazadaItemId(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        return data != null ? String.valueOf(data.get("item_id")) : null;
    }

    private String extractLazadaOrderId(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        return data != null ? String.valueOf(data.get("order_id")) : null;
    }

    private String extractTikTokProductId(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        return data != null ? String.valueOf(data.get("product_id")) : null;
    }

    private String extractTikTokOrderId(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        return data != null ? String.valueOf(data.get("order_id")) : null;
    }
}