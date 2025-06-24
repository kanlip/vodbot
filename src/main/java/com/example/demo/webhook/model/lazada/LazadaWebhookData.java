package com.example.demo.webhook.model.lazada;

import com.fasterxml.jackson.annotation.JsonAlias;

public record LazadaWebhookData<T>(
        @JsonAlias("seller_id")
        String sellerId,
        @JsonAlias("message_type")
        Integer messageType,
        T data,
        Long timestamp,
        String site
) {}


