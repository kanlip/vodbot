package com.example.demo.barcode.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Domain service responsible for barcode validation business logic
 */
@Service
@Slf4j
public class BarcodeValidationService {

    private final ProductPort productPort;

    public BarcodeValidationService(ProductPort productPort) {
        this.productPort = productPort;
    }

    /**
     * Validate if a barcode exists in the products database and is active
     */
    public boolean validateBarcode(String barcodeValue) {
        log.debug("Validating barcode: {}", barcodeValue);
        
        boolean isValid = productPort.isValidActiveBarcode(barcodeValue);
        
        if (isValid) {
            log.info("Barcode {} validated successfully", barcodeValue);
        } else {
            log.warn("Barcode {} not found or inactive in products database", barcodeValue);
        }
        
        return isValid;
    }
}