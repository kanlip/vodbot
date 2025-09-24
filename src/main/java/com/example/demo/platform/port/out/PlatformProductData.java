package com.example.demo.platform.port.out;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class PlatformProductData {
    private final String id;
    private final String sku;
    private final String name;
    private final String description;
    private final String category;
    private final String brand;
    private final BigDecimal price;
    private final String currency;
    private final Integer stockQuantity;
    private final String status;
    private final List<String> imageUrls;
    private final Map<String, Object> attributes;
    private final Map<String, Object> rawData;
}