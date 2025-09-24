package com.example.demo.platform.adapter.in.web;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BulkBarcodeGenerationResponse {
    private final int totalProducts;
    private final String message;
}