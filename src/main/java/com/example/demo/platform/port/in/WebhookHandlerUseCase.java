package com.example.demo.platform.port.in;

import com.example.demo.shared.domain.Platform;

import java.util.Map;

public interface WebhookHandlerUseCase {
    void processWebhook(Platform platform, String integrationId, Map<String, Object> payload, Map<String, String> headers);
    void retryFailedWebhooks();
}