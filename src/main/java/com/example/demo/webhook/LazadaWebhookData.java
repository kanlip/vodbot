package com.example.demo.webhook;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.jmolecules.event.types.DomainEvent;

public record LazadaWebhookData<T>(
        @JsonAlias("seller_id")
        String sellerId,
        @JsonAlias("message_type")
        Integer messageType,
        T data,
        Long timestamp,
        String site
) {}


