package com.example.demo.barcode.domain;

/**
 * Port interface for product-related operations in the barcode context
 */
public interface ProductPort {
    
    /**
     * Check if a barcode exists and is active in the products database
     */
    boolean isValidActiveBarcode(String barcodeValue);
    
    /**
     * Get product SKU for a barcode
     */
    String getSkuForBarcode(String barcodeValue);
    
    /**
     * Get barcode entity ID for linking
     */
    String getBarcodeEntityId(String barcodeValue);
}