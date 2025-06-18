package com.example.demo.integration.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Configuration
public class IntegrationConfiguration {

    private TikTokIntegrationProperties tikTokIntegrationProperties;


}
@Component
@ConfigurationProperties(prefix = "integration.tiktok")
class TikTokIntegrationProperties{
    String apiKey;
    String apiSecret;
    String accessToken;
    String openApiUrl;
    String shopCipher;
}