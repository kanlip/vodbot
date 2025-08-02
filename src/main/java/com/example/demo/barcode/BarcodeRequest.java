package com.example.demo.barcode;

import lombok.Data;

@Data
public class BarcodeRequest {

    private String barcodeValue;
    private ScanType scanType;
    private String packageId; // Optional: specify which package to scan for (useful for concurrent sessions)

    public enum ScanType {
        PACKAGE_START, // First scan - shipping label/packageId
        ITEM_VERIFICATION, // Subsequent scans - item verification
    }
}
