package com.example.demo.platform.adapter.in.web;

import com.example.demo.platform.domain.Product;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class ProductLookupResponse {
    private final UUID id;
    private final String masterSku;
    private final String productName;
    private final String description;
    private final String category;
    private final String brand;
    private final String barcode;
    private final Product.BarcodeType barcodeType;
    private final Instant barcodeGeneratedAt;
    private final Product.ProductStatus status;
    private final Map<String, Object> attributes;

    public static ProductLookupResponse from(Product product) {
        return ProductLookupResponse.builder()
                .id(product.getId())
                .masterSku(product.getMasterSku())
                .productName(product.getProductName())
                .description(product.getDescription())
                .category(product.getCategory())
                .brand(product.getBrand())
                .barcode(product.getBarcode())
                .barcodeType(product.getBarcodeType())
                .barcodeGeneratedAt(product.getBarcodeGeneratedAt())
                .status(product.getStatus())
                .attributes(product.getAttributes())
                .build();
    }
}