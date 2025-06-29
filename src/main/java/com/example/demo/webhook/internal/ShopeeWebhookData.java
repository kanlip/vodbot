package com.example.demo.webhook.internal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShopeeWebhookData<T>(
        @JsonAlias("shop_id")
        String shopId,
        T data,
        ShopeeMessageCode code,
        long timestamp
) {
}
