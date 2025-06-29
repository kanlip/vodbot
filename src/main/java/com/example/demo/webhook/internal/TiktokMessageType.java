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
@JsonDeserialize(using = TiktokMessageType.TiktokMessageTypeDeserializer.class)
public enum TiktokMessageType {

    ORDER_STATUS(1);

    private final int type;

    public static TiktokMessageType fromType(int type) {
        for (TiktokMessageType messageType : values()) {
            if (messageType.getType() == type) {
                return messageType;
            }
        }
        throw new IllegalArgumentException("Unknown Tiktok message type: " + type);
    }

    public static class TiktokMessageTypeDeserializer extends JsonDeserializer<TiktokMessageType> {
        @Override
        public TiktokMessageType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return fromType(p.getIntValue());
        }
    }
}
