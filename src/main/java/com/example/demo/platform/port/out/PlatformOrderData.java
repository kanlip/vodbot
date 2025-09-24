package com.example.demo.platform.port.out;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class PlatformOrderData {
    private final String id;
    private final String sellerId;
    private final String status;
    private final Instant orderDate;
    private final String customerName;
    private final String customerPhone;
    private final String customerEmail;
    private final Map<String, Object> shippingAddress;
    private final BigDecimal totalAmount;
    private final String currency;
    private final List<PlatformOrderItem> items;
    private final String specialInstructions;
    private final Map<String, Object> rawData;

    @Getter
    @Builder
    public static class PlatformOrderItem {
        private final String productId;
        private final String sku;
        private final String name;
        private final Integer quantity;
        private final BigDecimal unitPrice;
        private final Map<String, Object> attributes;
    }
}