package com.example.demo.platform.port.in;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class ImportProductCommand {
    private final UUID integrationId;
    private final String platformProductId;
    private final String platformSku;
    private final String productName;
    private final String description;
    private final String category;
    private final String brand;
    private final BigDecimal price;
    private final String currency;
    private final Integer stockQuantity;
    private final String platformBarcode; // Platform's own barcode if exists
    private final List<String> imageUrls;
    private final Map<String, Object> attributes;
    private final Map<String, Object> platformData;
}