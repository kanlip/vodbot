package com.example.demo.product.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class TikTokCommonParameter {
    @JsonProperty("shop_cipher")
    private String shopCipher;

    @JsonProperty("app_key")
    private String appKey;

    @JsonProperty("app_secret")
    private String appSecret;

    @JsonProperty("access_token")
    private String accessToken;

    private String timestamp;

    @JsonProperty("shop_id")
    private String shopId;

    private String version;

    private String sign;
}
