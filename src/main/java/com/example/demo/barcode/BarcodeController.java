package com.example.demo.barcode;

import com.example.demo.barcode.application.ItemVerificationService;
import com.example.demo.barcode.application.ScannedItemsQueryService;
import com.example.demo.barcode.domain.VideoPort;
import com.example.demo.common.IS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barcode")
@RequiredArgsConstructor
@Slf4j
public class BarcodeController {

    private final IS3Service s3Service;
    private final PackageStateService packageStateService;
    private final ItemVerificationService itemVerificationService;
    private final ScannedItemsQueryService scannedItemsQueryService;

    @PostMapping("/scan")
    public ResponseEntity<BarcodeResponse> handleBarcodeScanned(
        @RequestBody BarcodeRequest request
    ) {
        log.info(
            "Barcode scanned: {} with type: {}",
            request.getBarcodeValue(),
            request.getScanType()
        );

        try {
            // If no scan type specified, auto-determine based on barcode pattern and active packages
            BarcodeRequest.ScanType scanType = request.getScanType();
            if (scanType == null) {
                scanType = determineScanType(request.getBarcodeValue());
                log.info("Auto-determined scan type: {} for barcode: {}", scanType, request.getBarcodeValue());
            }

            if (scanType == BarcodeRequest.ScanType.PACKAGE_START) {
                return handlePackageStart(request.getBarcodeValue());
            } else {
                return handleItemVerification(request.getBarcodeValue(), request.getPackageId());
            }
        } catch (Exception e) {
            log.error("Error processing barcode scan: {}", e.getMessage(), e);

            BarcodeResponse errorResponse = BarcodeResponse.builder()
                .success(false)
                .message("Error processing barcode scan: " + e.getMessage())
                .barcodeValue(request.getBarcodeValue())
                .responseType(BarcodeResponse.ResponseType.ERROR)
                .shouldStartRecording(false)
                .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    private ResponseEntity<BarcodeResponse> handlePackageStart(
        String packageId
    ) {
        log.info("Processing package start for packageId: {}", packageId);

        // Check if this package is already active
        PackageStateService.PackageState existingPackage = 
            packageStateService.getActivePackage(packageId);
            
        if (existingPackage != null) {
            log.info("Package {} is already active, returning existing session", packageId);
            BarcodeResponse response = BarcodeResponse.builder()
                .success(true)
                .message("Package recording session already active. Continue scanning items to verify.")
                .presignedUrl(existingPackage.getPresignedUrl())
                .barcodeValue(packageId)
                .responseType(BarcodeResponse.ResponseType.PACKAGE_STARTED)
                .shouldStartRecording(false) // Recording should already be started
                .build();
            return ResponseEntity.ok(response);
        }

        // Generate presigned URL for the package
        String presignedUrl = s3Service.getPresignedUriForPackage(packageId);

        // Start the package session
        PackageStateService.PackageState packageState =
            packageStateService.startPackage(packageId, presignedUrl);

        BarcodeResponse response = BarcodeResponse.builder()
            .success(true)
            .message(
                "Package recording started. Begin scanning items to verify."
            )
            .presignedUrl(presignedUrl)
            .barcodeValue(packageId)
            .responseType(BarcodeResponse.ResponseType.PACKAGE_STARTED)
            .shouldStartRecording(true)
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Determines scan type based on barcode value and existing package state
     */
    private BarcodeRequest.ScanType determineScanType(String barcodeValue) {
        // First check if this barcode is already an active package
        PackageStateService.PackageState existingPackage = 
            packageStateService.getActivePackage(barcodeValue);
        if (existingPackage != null) {
            // This package is already active, so this is a package start (re-scan)
            return BarcodeRequest.ScanType.PACKAGE_START;
        }
        
        // Check if this is a valid product barcode
        boolean isValidProduct = itemVerificationService.isValidProductBarcode(barcodeValue);
        if (isValidProduct) {
            // This is a product barcode, so it's item verification
            return BarcodeRequest.ScanType.ITEM_VERIFICATION;
        }
        
        // If it's not a known product barcode, assume it's a package start
        // This covers shipping labels, package IDs, etc.
        return BarcodeRequest.ScanType.PACKAGE_START;
    }

    private ResponseEntity<BarcodeResponse> handleItemVerification(
        String itemBarcode, 
        String requestedPackageId
    ) {
        log.info("Processing item verification for item: {}, requested package: {}", itemBarcode, requestedPackageId);

        // Determine which package to use for this item scan
        PackageStateService.PackageState targetPackage = null;
        
        if (requestedPackageId != null) {
            // Use the explicitly requested package
            targetPackage = packageStateService.getActivePackage(requestedPackageId);
            if (targetPackage == null) {
                BarcodeResponse response = BarcodeResponse.builder()
                    .success(false)
                    .message(String.format("Requested package %s is not active. Please start recording first.", requestedPackageId))
                    .barcodeValue(itemBarcode)
                    .responseType(BarcodeResponse.ResponseType.ERROR)
                    .shouldStartRecording(false)
                    .build();
                return ResponseEntity.badRequest().body(response);
            }
        } else {
            // No specific package requested, use the most recently started package
            targetPackage = packageStateService.getMostRecentActivePackage();
            if (targetPackage == null) {
                BarcodeResponse response = BarcodeResponse.builder()
                    .success(false)
                    .message("No active package found. Please scan a shipping label first.")
                    .barcodeValue(itemBarcode)
                    .responseType(BarcodeResponse.ResponseType.ERROR)
                    .shouldStartRecording(false)
                    .build();
                return ResponseEntity.badRequest().body(response);
            }
            log.info("Using most recent active package: {}", targetPackage.getPackageId());
        }

        // Use application service to verify and save item
        ItemVerificationService.ItemVerificationResult result = 
            itemVerificationService.verifyAndSaveItem(targetPackage.getPackageId(), itemBarcode);

        BarcodeResponse response = BarcodeResponse.builder()
            .success(result.isSuccess())
            .message(result.getMessage())
            .barcodeValue(result.getBarcodeValue())
            .responseType(result.isSuccess() ? 
                BarcodeResponse.ResponseType.ITEM_VERIFIED : 
                BarcodeResponse.ResponseType.ERROR)
            .shouldStartRecording(false)
            .presignedUrl(result.isSuccess() ? targetPackage.getPresignedUrl() : null)
            .build();

        return result.isSuccess() ? 
            ResponseEntity.ok(response) : 
            ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/end-package")
    public ResponseEntity<BarcodeResponse> endPackage(
        @RequestParam String packageId
    ) {
        log.info("Ending package: {}", packageId);
        packageStateService.endPackage(packageId);

        BarcodeResponse response = BarcodeResponse.builder()
            .success(true)
            .message("Package session ended")
            .barcodeValue(packageId)
            .responseType(BarcodeResponse.ResponseType.PACKAGE_STARTED)
            .shouldStartRecording(false)
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/package/{packageId}/scanned-items")
    public ResponseEntity<?> getScannedItems(@PathVariable String packageId) {
        log.info("Getting scanned items for package: {}", packageId);
        
        try {
            List<VideoPort.ItemScanData> scannedItems = scannedItemsQueryService.getScannedItems(packageId);
            return ResponseEntity.ok(scannedItems);
        } catch (ScannedItemsQueryService.ScannedItemsQueryException e) {
            log.error("Error retrieving scanned items for package {}: {}", packageId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
