package com.example.demo.platform.adapter.in.web;

import com.example.demo.platform.domain.BarcodeGenerationLog;
import com.example.demo.platform.domain.Product;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class BarcodeHistoryResponse {
    private final UUID id;
    private final String barcode;
    private final Product.BarcodeType barcodeType;
    private final BarcodeGenerationLog.GenerationMethod generationMethod;
    private final UUID generatedBy;
    private final Instant createdAt;

    public static BarcodeHistoryResponse from(BarcodeGenerationLog log) {
        return BarcodeHistoryResponse.builder()
                .id(log.getId())
                .barcode(log.getBarcode())
                .barcodeType(log.getBarcodeType())
                .generationMethod(log.getGenerationMethod())
                .generatedBy(log.getGeneratedBy())
                .createdAt(log.getCreatedAt())
                .build();
    }
}