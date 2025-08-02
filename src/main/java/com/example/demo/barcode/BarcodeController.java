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
            // If no scan type specified, auto-determine based on active packages
            BarcodeRequest.ScanType scanType = request.getScanType();
            if (scanType == null) {
                // If there's no active package, this is a package start
                // If there's an active package, this is item verification
                scanType = packageStateService.getAnyActivePackage() == null
                    ? BarcodeRequest.ScanType.PACKAGE_START
                    : BarcodeRequest.ScanType.ITEM_VERIFICATION;
                log.info("Auto-determined scan type: {}", scanType);
            }

            if (scanType == BarcodeRequest.ScanType.PACKAGE_START) {
                return handlePackageStart(request.getBarcodeValue());
            } else {
                return handleItemVerification(request.getBarcodeValue());
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

        // TODO - solve real package with multiple sessions

        // Clear any existing packages (for simplicity, only one active package at a time)
        packageStateService.clearAllPackages();

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

    private ResponseEntity<BarcodeResponse> handleItemVerification(
        String itemBarcode
    ) {
        log.info("Processing item verification for item: {}", itemBarcode);

        // Get the active package
        PackageStateService.PackageState activePackage =
            packageStateService.getAnyActivePackage();

        if (activePackage == null) {
            BarcodeResponse response = BarcodeResponse.builder()
                .success(false)
                .message(
                    "No active package found. Please scan a shipping label first."
                )
                .barcodeValue(itemBarcode)
                .responseType(BarcodeResponse.ResponseType.ERROR)
                .shouldStartRecording(false)
                .build();

            return ResponseEntity.badRequest().body(response);
        }

        // Use application service to verify and save item
        ItemVerificationService.ItemVerificationResult result = 
            itemVerificationService.verifyAndSaveItem(activePackage.getPackageId(), itemBarcode);

        BarcodeResponse response = BarcodeResponse.builder()
            .success(result.isSuccess())
            .message(result.getMessage())
            .barcodeValue(result.getBarcodeValue())
            .responseType(result.isSuccess() ? 
                BarcodeResponse.ResponseType.ITEM_VERIFIED : 
                BarcodeResponse.ResponseType.ERROR)
            .shouldStartRecording(false)
            .presignedUrl(result.isSuccess() ? activePackage.getPresignedUrl() : null)
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
