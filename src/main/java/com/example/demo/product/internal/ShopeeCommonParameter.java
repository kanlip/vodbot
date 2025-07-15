package com.example.demo.product.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
public class ShopeeCommonParameter
{
    private String partnerId;
    private String timestamp;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("shop_id")
    private String shopId;
    private String sign;
}
