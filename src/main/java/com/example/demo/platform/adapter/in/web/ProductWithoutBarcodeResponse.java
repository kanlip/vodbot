package com.example.demo.platform.adapter.in.web;

import com.example.demo.platform.domain.Product;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ProductWithoutBarcodeResponse {
    private final UUID id;
    private final String masterSku;
    private final String productName;
    private final String category;
    private final String brand;

    public static ProductWithoutBarcodeResponse from(Product product) {
        return ProductWithoutBarcodeResponse.builder()
                .id(product.getId())
                .masterSku(product.getMasterSku())
                .productName(product.getProductName())
                .category(product.getCategory())
                .brand(product.getBrand())
                .build();
    }
}