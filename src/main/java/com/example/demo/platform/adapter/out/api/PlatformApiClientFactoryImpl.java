package com.example.demo.platform.adapter.out.api;

import com.example.demo.platform.domain.PlatformIntegration;
import com.example.demo.platform.port.out.PlatformApiClient;
import com.example.demo.platform.port.out.PlatformApiClientFactory;
import com.example.demo.shared.domain.Platform;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlatformApiClientFactoryImpl implements PlatformApiClientFactory {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public PlatformApiClient createClient(Platform platform, PlatformIntegration integration) {
        log.info("Creating API client for platform: {} and integration: {}", platform, integration.getId());

        if (!integration.isAuthorized()) {
            throw new IllegalStateException("Integration is not authorized: " + integration.getId());
        }

        return switch (platform) {
            case SHOPEE -> new ShopeeApiClient(restTemplate, objectMapper, integration);
            case LAZADA -> new LazadaApiClient(restTemplate, objectMapper, integration);
            case TIKTOK -> new TikTokShopApiClient(restTemplate, objectMapper, integration);
        };
    }
}