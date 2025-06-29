package com.example.demo.webhook.internal;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
@Getter
@JsonDeserialize(using = ShopeeMessageCode.ShopeeMessageCodeDeserializer.class)
public enum ShopeeMessageCode {
    ORDER_STATUS_PUSH(3),
    TRACKING_NUMBER_PUSH(4),
    SHIPPING_DOCUMENT_STATUS_PUSH(15),
    PACKAGE_FULFILLMENT_STATUS_PUSH(30);

    private final int code;

    public static ShopeeMessageCode fromCode(int code) {
        for (ShopeeMessageCode messageCode : values()) {
            if (messageCode.getCode() == code) {
                return messageCode;
            }
        }
        throw new IllegalArgumentException("Unknown message code: " + code);
    }

    public static class ShopeeMessageCodeDeserializer extends JsonDeserializer<ShopeeMessageCode> {
        @Override
        public ShopeeMessageCode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return fromCode(p.getIntValue());
        }
    }
}
