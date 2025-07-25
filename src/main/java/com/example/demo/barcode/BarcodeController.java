package com.example.demo.barcode;

import com.example.demo.common.IS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/barcode")
@RequiredArgsConstructor
@Slf4j
public class BarcodeController {

    private final IS3Service s3Service;

    @PostMapping("/scan")
    public ResponseEntity<BarcodeResponse> handleBarcodeScanned(@RequestBody BarcodeRequest request) {
        log.info("Barcode scanned: {}", request.getBarcodeValue());
        
        try {
            // Generate presigned URL for the scanned barcode
            String presignedUrl = s3Service.getPresignedUriForBarcode(request.getBarcodeValue());
            
            BarcodeResponse response = BarcodeResponse.builder()
                    .success(true)
                    .message("Presigned URL generated successfully")
                    .presignedUrl(presignedUrl)
                    .barcodeValue(request.getBarcodeValue())
                    .build();
                    
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing barcode scan: {}", e.getMessage(), e);
            
            BarcodeResponse errorResponse = BarcodeResponse.builder()
                    .success(false)
                    .message("Error generating presigned URL: " + e.getMessage())
                    .barcodeValue(request.getBarcodeValue())
                    .build();
                    
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}