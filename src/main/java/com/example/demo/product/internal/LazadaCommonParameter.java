package com.example.demo.product.internal;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class LazadaCommonParameter {
    @JsonProperty("app_key")
    private String appKey;
    private String timestamp;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("sign_method")
    private String signMethod;
    private String sign;
}
