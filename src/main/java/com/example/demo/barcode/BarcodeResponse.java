package com.example.demo.barcode;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BarcodeResponse {
    private boolean success;
    private String message;
    private String presignedUrl;
    private String barcodeValue;
}