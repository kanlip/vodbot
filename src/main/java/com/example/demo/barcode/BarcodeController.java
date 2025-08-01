package com.example.demo.barcode;

import com.example.demo.common.IS3Service;
import com.example.demo.product.IProductRepository;
import com.example.demo.product.entity.BarcodeEntity;
import com.example.demo.video.entity.VideoEntity;
import com.example.demo.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/barcode")
@RequiredArgsConstructor
@Slf4j
public class BarcodeController {

    private final IS3Service s3Service;
    private final PackageStateService packageStateService;
    private final VideoRepository videoRepository;
    private final IProductRepository productRepository;

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

        try {
            // Validate the scanned item against products database
            boolean isValidItem = validateScannedItem(itemBarcode);

            if (!isValidItem) {
                BarcodeResponse response = BarcodeResponse.builder()
                    .success(false)
                    .message(
                        String.format(
                            "Item %s is not found or inactive in products database",
                            itemBarcode
                        )
                    )
                    .barcodeValue(itemBarcode)
                    .responseType(BarcodeResponse.ResponseType.ERROR)
                    .shouldStartRecording(false)
                    .build();

                return ResponseEntity.badRequest().body(response);
            }

            // Save the scanned item to the database
            saveItemScanToVideo(activePackage.getPackageId(), itemBarcode);

            log.info(
                "Item {} successfully verified and saved for package {}",
                itemBarcode,
                activePackage.getPackageId()
            );

            BarcodeResponse response = BarcodeResponse.builder()
                .success(true)
                .message(
                    String.format(
                        "Item %s verified and saved for package %s",
                        itemBarcode,
                        activePackage.getPackageId()
                    )
                )
                .barcodeValue(itemBarcode)
                .responseType(BarcodeResponse.ResponseType.ITEM_VERIFIED)
                .shouldStartRecording(false)
                .presignedUrl(activePackage.getPresignedUrl()) // Include the presigned URL from the active package
                .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error verifying and saving item {}: {}", itemBarcode, e.getMessage(), e);
            
            BarcodeResponse response = BarcodeResponse.builder()
                .success(false)
                .message("Error processing item scan: " + e.getMessage())
                .barcodeValue(itemBarcode)
                .responseType(BarcodeResponse.ResponseType.ERROR)
                .shouldStartRecording(false)
                .build();

            return ResponseEntity.badRequest().body(response);
        }
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
            var scannedItems = getScannedItemsForPackage(packageId);
            return ResponseEntity.ok(scannedItems);
        } catch (Exception e) {
            log.error("Error retrieving scanned items for package {}: {}", packageId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error retrieving scanned items: " + e.getMessage());
        }
    }

    /**
     * Validate if a barcode exists in the products database and is active
     */
    private boolean validateScannedItem(String barcodeValue) {
        log.debug("Validating barcode: {}", barcodeValue);
        
        List<BarcodeEntity> allBarcodes = productRepository.findAll();
        boolean isValid = allBarcodes.stream()
                .anyMatch(barcode -> barcodeValue.equals(barcode.getBarcodeValue()) && 
                                   "active".equals(barcode.getStatus()));
        
        if (isValid) {
            log.info("Barcode {} validated successfully", barcodeValue);
        } else {
            log.warn("Barcode {} not found or inactive in products database", barcodeValue);
        }
        
        return isValid;
    }

    /**
     * Save the scanned item to the video entity in the database
     */
    private void saveItemScanToVideo(String packageId, String barcodeValue) {
        log.info("Saving item scan to video for package: {}, barcode: {}", packageId, barcodeValue);
        
        // Find or create video entity for this package
        VideoEntity videoEntity = findOrCreateVideoForPackage(packageId);
        
        // Get barcode entity for additional details
        BarcodeEntity barcodeEntity = findBarcodeEntity(barcodeValue);
        
        // Create item scan record
        VideoEntity.ItemScan itemScan = new VideoEntity.ItemScan();
        itemScan.setTimestampOffsetSeconds(0); // Default timestamp
        itemScan.setSku(barcodeEntity != null ? barcodeEntity.getPlatformSkuId() : barcodeValue);
        itemScan.setQuantity(1); // Default quantity
        itemScan.setStatus(barcodeEntity != null ? "verified" : "unknown");
        if (barcodeEntity != null) {
            itemScan.setBarcodeEntityId(barcodeEntity.getId());
        }
        
        // Add to video entity
        if (videoEntity.getItemScans() == null) {
            videoEntity.setItemScans(new ArrayList<>());
        }
        videoEntity.getItemScans().add(itemScan);
        videoEntity.setUpdatedAt(Instant.now());
        
        // Save video entity
        videoRepository.save(videoEntity);
        
        log.info("Item scan saved successfully: {}", itemScan);
    }

    /**
     * Find existing video entity for package or create a new one
     */
    private VideoEntity findOrCreateVideoForPackage(String packageId) {
        // Try to find existing video entity by platform order ID
        Optional<VideoEntity> existingVideo = videoRepository.findAll().stream()
                .filter(video -> packageId.equals(video.getPlatformOrderId()))
                .findFirst();
        
        if (existingVideo.isPresent()) {
            log.debug("Found existing video entity for package: {}", packageId);
            return existingVideo.get();
        }
        
        // Create new video entity
        log.info("Creating new video entity for package: {}", packageId);
        VideoEntity newVideo = new VideoEntity();
        newVideo.setPlatformOrderId(packageId);
        newVideo.setStatus("recording");
        newVideo.setCreatedAt(Instant.now());
        newVideo.setUpdatedAt(Instant.now());
        newVideo.setItemScans(new ArrayList<>());
        
        return videoRepository.save(newVideo);
    }

    /**
     * Find barcode entity by barcode value
     */
    private BarcodeEntity findBarcodeEntity(String barcodeValue) {
        List<BarcodeEntity> allBarcodes = productRepository.findAll();
        return allBarcodes.stream()
                .filter(barcode -> barcodeValue.equals(barcode.getBarcodeValue()))
                .filter(barcode -> "active".equals(barcode.getStatus()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all scanned items for a package
     */
    private List<VideoEntity.ItemScan> getScannedItemsForPackage(String packageId) {
        Optional<VideoEntity> video = videoRepository.findAll().stream()
                .filter(v -> packageId.equals(v.getPlatformOrderId()))
                .findFirst();
        
        return video.map(VideoEntity::getItemScans).orElse(new ArrayList<>());
    }
}
