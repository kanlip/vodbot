package com.example.demo.webhook.service;


import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookEvent {

    private final ApplicationEventPublisher events;
    public <T> void publishWebhookEvent(T event) {
        events.publishEvent(event);
    }
}
