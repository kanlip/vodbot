package com.example.demo.platform.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "platform")
public class PlatformOAuthProperties {

    @NestedConfigurationProperty
    private OAuth oauth = new OAuth();

    @NestedConfigurationProperty
    private Lazada lazada = new Lazada();

    @NestedConfigurationProperty
    private Shopee shopee = new Shopee();

    @NestedConfigurationProperty
    private Tiktok tiktok = new Tiktok();

    @Data
    public static class OAuth {
        private String callbackUrl = "http://localhost:8080/platform/callback";
    }

    @Data
    public static class Lazada {
        @NestedConfigurationProperty
        private OAuthEndpoints oauth = new OAuthEndpoints();
        @NestedConfigurationProperty
        private AppCredentials app = new AppCredentials();
    }

    @Data
    public static class Shopee {
        @NestedConfigurationProperty
        private OAuthEndpoints oauth = new OAuthEndpoints();
        @NestedConfigurationProperty
        private ShopeeApp app = new ShopeeApp();
        private String accessToken;
    }

    @Data
    public static class Tiktok {
        @NestedConfigurationProperty
        private OAuthEndpoints oauth = new OAuthEndpoints();
        @NestedConfigurationProperty
        private AppCredentials app = new AppCredentials();
        private String shopCipher;
        private String openApiUrl;
        private String accessToken;
    }

    @Data
    public static class OAuthEndpoints {
        private String authUrl;
        private String tokenUrl;
        private String refreshUrl;
    }

    @Data
    public static class AppCredentials {
        private String key;
        private String secret;
    }

    @Data
    public static class ShopeeApp {
        private String partnerId;
        private String secret;
    }
}