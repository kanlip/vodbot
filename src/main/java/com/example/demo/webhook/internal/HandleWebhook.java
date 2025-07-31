package com.example.demo.webhook.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class HandleWebhook {

    private final @NonNull ApplicationEventPublisher eventPublisher;
    private final ObjectMapper mapper;

    public <T, U> void handle(T data, Class<U> castType) {
        // Logic to handle the webhook payload

        U tradeOrder = mapper.convertValue(data, castType);
        log.info("Handling Webhook Data: {}", tradeOrder);
        eventPublisher.publishEvent(tradeOrder);
    }
}
