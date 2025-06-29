package com.example.demo.webhook.internal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TiktokWebhookData<T>(
        @JsonAlias("shop_id")
        String shopId,
        long timestamp,
        T data,
        TiktokMessageType type,
        @JsonAlias("tts_notification_id")
        String ttsNotificationId
) {
}
