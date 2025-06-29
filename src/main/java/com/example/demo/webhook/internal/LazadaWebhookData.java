package com.example.demo.webhook.internal;

import com.fasterxml.jackson.annotation.JsonAlias;

public record LazadaWebhookData<T>(
        @JsonAlias("seller_id")
        String sellerId,
        @JsonAlias("message_type")
        LazadaMessageType messageType,
        T data,
        long timestamp,
        String site
) {}


