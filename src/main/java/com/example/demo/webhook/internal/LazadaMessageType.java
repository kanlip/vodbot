package com.example.demo.webhook.internal;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonDeserialize(using = LazadaMessageType.LazadaMessageTypeDeserializer.class)
public enum LazadaMessageType {

    TRADE_ORDER_NOTIFICATION(0),
    REVERSE_ORDER(10);

    private final int messageType;

    public static LazadaMessageType fromMessageType(int messageType) {
        for (LazadaMessageType type : values()) {
            if (type.getMessageType() == messageType) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + messageType);
    }
    public static class LazadaMessageTypeDeserializer extends JsonDeserializer<LazadaMessageType> {
        @Override
        public LazadaMessageType deserialize(JsonParser p, DeserializationContext ctxt) throws java.io.IOException {
            return fromMessageType(p.getIntValue());
        }
    }
}
