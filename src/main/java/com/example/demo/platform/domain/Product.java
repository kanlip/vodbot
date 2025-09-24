package com.example.demo.platform.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private UUID id;
    private UUID orgId;
    private String masterSku;
    private String productName;
    private String description;
    private String category;
    private String brand;
    private Integer weightGrams;
    private Dimensions dimensions;
    private Map<String, Object> attributes;
    private String primaryImageUrl;
    private String barcode;
    private BarcodeType barcodeType;
    private Instant barcodeGeneratedAt;
    private ProductStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private List<ProductImage> images;
    private List<PlatformProduct> platformMappings;

    public enum ProductStatus {
        ACTIVE,
        INACTIVE,
        DISCONTINUED
    }

    public enum BarcodeType {
        CODE128,
        CODE39,
        EAN13,
        EAN8,
        UPC_A,
        UPC_E
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dimensions {
        private Double lengthCm;
        private Double widthCm;
        private Double heightCm;

        public Double getVolumeCubicCm() {
            if (lengthCm != null && widthCm != null && heightCm != null) {
                return lengthCm * widthCm * heightCm;
            }
            return null;
        }
    }

    public boolean hasAttribute(String key) {
        return attributes != null && attributes.containsKey(key);
    }

    public Object getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }

    public String getAttributeAsString(String key) {
        Object value = getAttribute(key);
        return value != null ? value.toString() : null;
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == ProductStatus.ACTIVE;
    }

    public void updateBasicInfo(String productName, String description, String category) {
        this.productName = productName;
        this.description = description;
        this.category = category;
        this.updatedAt = Instant.now();
    }

    public boolean hasBarcode() {
        return barcode != null && !barcode.isEmpty();
    }

    public void assignBarcode(String barcode, BarcodeType type) {
        this.barcode = barcode;
        this.barcodeType = type;
        this.barcodeGeneratedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean needsBarcode() {
        return !hasBarcode() && isActive();
    }
}