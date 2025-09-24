package com.example.demo.platform.adapter.in.web;

import com.example.demo.platform.port.in.WebhookHandlerUseCase;
import com.example.demo.shared.domain.Platform;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Platform Webhooks", description = "Webhook endpoints for platform integrations")
public class WebhookController {

    private final WebhookHandlerUseCase webhookHandler;

    @PostMapping("/shopee/{integrationId}")
    @Operation(summary = "Handle Shopee webhooks")
    public ResponseEntity<?> handleShopeeWebhook(
            @PathVariable String integrationId,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        log.info("Received Shopee webhook for integration: {}", integrationId);

        try {
            Map<String, String> headers = getHeaders(request);
            webhookHandler.processWebhook(
                    Platform.SHOPEE,
                    integrationId,
                    payload,
                    headers
            );

            return ResponseEntity.ok(Collections.singletonMap("status", "success"));

        } catch (Exception e) {
            log.error("Failed to process Shopee webhook for integration {}: {}", integrationId, e.getMessage());
            return ResponseEntity.ok(Collections.singletonMap("status", "error"));
        }
    }

    @PostMapping("/lazada/{integrationId}")
    @Operation(summary = "Handle Lazada webhooks")
    public ResponseEntity<?> handleLazadaWebhook(
            @PathVariable String integrationId,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        log.info("Received Lazada webhook for integration: {}", integrationId);

        try {
            Map<String, String> headers = getHeaders(request);
            webhookHandler.processWebhook(
                    Platform.LAZADA,
                    integrationId,
                    payload,
                    headers
            );

            return ResponseEntity.ok(Collections.singletonMap("status", "success"));

        } catch (Exception e) {
            log.error("Failed to process Lazada webhook for integration {}: {}", integrationId, e.getMessage());
            return ResponseEntity.ok(Collections.singletonMap("status", "error"));
        }
    }

    @PostMapping("/tiktok/{integrationId}")
    @Operation(summary = "Handle TikTok Shop webhooks")
    public ResponseEntity<?> handleTikTokWebhook(
            @PathVariable String integrationId,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        log.info("Received TikTok webhook for integration: {}", integrationId);

        try {
            Map<String, String> headers = getHeaders(request);
            webhookHandler.processWebhook(
                    Platform.TIKTOK,
                    integrationId,
                    payload,
                    headers
            );

            return ResponseEntity.ok(Collections.singletonMap("status", "success"));

        } catch (Exception e) {
            log.error("Failed to process TikTok webhook for integration {}: {}", integrationId, e.getMessage());
            return ResponseEntity.ok(Collections.singletonMap("status", "error"));
        }
    }

    @GetMapping("/shopee/{integrationId}/verify")
    @Operation(summary = "Verify Shopee webhook endpoint")
    public ResponseEntity<String> verifyShopeeWebhook(
            @PathVariable String integrationId,
            @RequestParam(required = false) String challenge) {

        log.info("Verifying Shopee webhook endpoint for integration: {}", integrationId);

        if (challenge != null) {
            return ResponseEntity.ok(challenge);
        }

        return ResponseEntity.ok("Webhook endpoint verified");
    }

    @GetMapping("/lazada/{integrationId}/verify")
    @Operation(summary = "Verify Lazada webhook endpoint")
    public ResponseEntity<String> verifyLazadaWebhook(
            @PathVariable String integrationId,
            @RequestParam(required = false) String challenge) {

        log.info("Verifying Lazada webhook endpoint for integration: {}", integrationId);

        if (challenge != null) {
            return ResponseEntity.ok(challenge);
        }

        return ResponseEntity.ok("Webhook endpoint verified");
    }

    @GetMapping("/tiktok/{integrationId}/verify")
    @Operation(summary = "Verify TikTok webhook endpoint")
    public ResponseEntity<String> verifyTikTokWebhook(
            @PathVariable String integrationId,
            @RequestParam(required = false) String challenge) {

        log.info("Verifying TikTok webhook endpoint for integration: {}", integrationId);

        if (challenge != null) {
            return ResponseEntity.ok(challenge);
        }

        return ResponseEntity.ok("Webhook endpoint verified");
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        headerName -> headerName,
                        request::getHeader
                ));
    }
}