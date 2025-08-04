package com.example.demo.barcode.application;

import com.example.demo.barcode.domain.BarcodeValidationService;
import com.example.demo.barcode.domain.ProductPort;
import com.example.demo.barcode.domain.VideoPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Application service for item verification use case
 */
@Service
@Slf4j
public class ItemVerificationService {

    private final BarcodeValidationService barcodeValidationService;
    private final ProductPort productPort;
    private final VideoPort videoPort;

    public ItemVerificationService(BarcodeValidationService barcodeValidationService, 
                                 ProductPort productPort, 
                                 VideoPort videoPort) {
        this.barcodeValidationService = barcodeValidationService;
        this.productPort = productPort;
        this.videoPort = videoPort;
    }

    /**
     * Verify an item barcode and save to video if valid
     */
    public ItemVerificationResult verifyAndSaveItem(String packageId, String itemBarcode) {
        log.info("Verifying and saving item {} for package {}", itemBarcode, packageId);

        try {
            // Validate the scanned item
            boolean isValid = barcodeValidationService.validateBarcode(itemBarcode);

            if (!isValid) {
                return ItemVerificationResult.failure(
                    String.format("Item %s is not found or inactive in products database", itemBarcode),
                    itemBarcode
                );
            }

            // Create item scan data
            VideoPort.ItemScanData itemScan = createItemScanData(itemBarcode);
            
            // Save to video entity
            videoPort.saveItemScanToVideo(packageId, itemScan);

            log.info("Item {} successfully verified and saved for package {}", itemBarcode, packageId);

            return ItemVerificationResult.success(
                String.format("Item %s verified and saved for package %s", itemBarcode, packageId),
                itemBarcode
            );

        } catch (Exception e) {
            log.error("Error verifying and saving item {}: {}", itemBarcode, e.getMessage(), e);
            return ItemVerificationResult.failure(
                "Error processing item scan: " + e.getMessage(),
                itemBarcode
            );
        }
    }

    /**
     * Check if a barcode is a valid product barcode (used for scan type determination)
     */
    public boolean isValidProductBarcode(String barcodeValue) {
        return barcodeValidationService.validateBarcode(barcodeValue);
    }

    private VideoPort.ItemScanData createItemScanData(String barcodeValue) {
        String sku = productPort.getSkuForBarcode(barcodeValue);
        String barcodeEntityId = productPort.getBarcodeEntityId(barcodeValue);
        
        return new VideoPort.ItemScanData(
            0, // Default timestamp
            sku != null ? sku : barcodeValue,
            1, // Default quantity
            "verified",
            barcodeEntityId
        );
    }

    /**
     * Result of item verification operation
     */
    public static class ItemVerificationResult {
        private final boolean success;
        private final String message;
        private final String barcodeValue;

        private ItemVerificationResult(boolean success, String message, String barcodeValue) {
            this.success = success;
            this.message = message;
            this.barcodeValue = barcodeValue;
        }

        public static ItemVerificationResult success(String message, String barcodeValue) {
            return new ItemVerificationResult(true, message, barcodeValue);
        }

        public static ItemVerificationResult failure(String message, String barcodeValue) {
            return new ItemVerificationResult(false, message, barcodeValue);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getBarcodeValue() { return barcodeValue; }
    }
}