package com.example.demo.barcode;

import com.example.demo.common.IS3Service;
import com.example.demo.product.IProductRepository;
import com.example.demo.product.entity.BarcodeEntity;
import com.example.demo.video.entity.VideoEntity;
import com.example.demo.video.repository.VideoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BarcodeController.class)
class BarcodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IS3Service s3Service;
    
    @MockBean
    private PackageStateService packageStateService;

    @MockBean
    private VideoRepository videoRepository;

    @MockBean
    private IProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldStartPackageForFirstScan() throws Exception {
        // Given
        String testPackageId = "PKG123456789";
        String mockPresignedUrl = "https://test-bucket.s3.amazonaws.com/package-presigned-url";
        BarcodeRequest request = new BarcodeRequest();
        request.setBarcodeValue(testPackageId);

        // Mock no active packages (first scan)
        when(packageStateService.getAnyActivePackage()).thenReturn(null);
        when(s3Service.getPresignedUriForPackage(testPackageId)).thenReturn(mockPresignedUrl);
        when(packageStateService.startPackage(testPackageId, mockPresignedUrl))
                .thenReturn(new PackageStateService.PackageState(testPackageId, mockPresignedUrl));

        // When & Then
        mockMvc.perform(post("/api/barcode/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Package recording started. Begin scanning items to verify."))
                .andExpect(jsonPath("$.presignedUrl").value(mockPresignedUrl))
                .andExpect(jsonPath("$.barcodeValue").value(testPackageId))
                .andExpect(jsonPath("$.responseType").value("PACKAGE_STARTED"))
                .andExpect(jsonPath("$.shouldStartRecording").value(true));
    }

    @Test
    void shouldVerifyItemForSubsequentScan() throws Exception {
        // Given
        String testItemBarcode = "ITEM123456789";
        String testPackageId = "PKG123456789";
        String mockPresignedUrl = "https://test-bucket.s3.amazonaws.com/package-presigned-url";
        BarcodeRequest request = new BarcodeRequest();
        request.setBarcodeValue(testItemBarcode);

        // Mock active package exists (subsequent scan)
        PackageStateService.PackageState mockPackageState = new PackageStateService.PackageState(testPackageId, mockPresignedUrl);
        when(packageStateService.getAnyActivePackage()).thenReturn(mockPackageState);
        
        // Mock barcode validation - create valid barcode entity
        BarcodeEntity validBarcode = BarcodeEntity.builder()
                .id(new ObjectId())
                .barcodeValue(testItemBarcode)
                .status("active")
                .platformSkuId("SKU123")
                .build();
        when(productRepository.findAll()).thenReturn(List.of(validBarcode));
        
        // Mock video repository - return existing video
        VideoEntity existingVideo = new VideoEntity();
        existingVideo.setPlatformOrderId(testPackageId);
        existingVideo.setItemScans(new ArrayList<>());
        existingVideo.setCreatedAt(Instant.now());
        existingVideo.setUpdatedAt(Instant.now());
        when(videoRepository.findAll()).thenReturn(List.of(existingVideo));
        when(videoRepository.save(any(VideoEntity.class))).thenReturn(existingVideo);

        // When & Then
        mockMvc.perform(post("/api/barcode/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item " + testItemBarcode + " verified and saved for package " + testPackageId))
                .andExpect(jsonPath("$.presignedUrl").value(mockPresignedUrl))
                .andExpect(jsonPath("$.barcodeValue").value(testItemBarcode))
                .andExpect(jsonPath("$.responseType").value("ITEM_VERIFIED"))
                .andExpect(jsonPath("$.shouldStartRecording").value(false));
    }

    @Test
    void shouldReturnErrorWhenS3ServiceFails() throws Exception {
        // Given
        String testPackageId = "PKG123456789";
        BarcodeRequest request = new BarcodeRequest();
        request.setBarcodeValue(testPackageId);

        // Mock no active packages (first scan)
        when(packageStateService.getAnyActivePackage()).thenReturn(null);
        when(s3Service.getPresignedUriForPackage(testPackageId))
                .thenThrow(new RuntimeException("S3 service error"));

        // When & Then
        mockMvc.perform(post("/api/barcode/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error processing barcode scan: S3 service error"))
                .andExpect(jsonPath("$.barcodeValue").value(testPackageId))
                .andExpect(jsonPath("$.responseType").value("ERROR"));
    }

    @Test
    void shouldReturnErrorForInvalidItem() throws Exception {
        // Given
        String testItemBarcode = "INVALID123";
        String testPackageId = "PKG123456789";
        String mockPresignedUrl = "https://test-bucket.s3.amazonaws.com/package-presigned-url";
        BarcodeRequest request = new BarcodeRequest();
        request.setBarcodeValue(testItemBarcode);

        // Mock active package exists
        PackageStateService.PackageState mockPackageState = new PackageStateService.PackageState(testPackageId, mockPresignedUrl);
        when(packageStateService.getAnyActivePackage()).thenReturn(mockPackageState);
        
        // Mock barcode validation - return empty list (no valid barcodes)
        when(productRepository.findAll()).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(post("/api/barcode/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Item " + testItemBarcode + " is not found or inactive in products database"))
                .andExpect(jsonPath("$.barcodeValue").value(testItemBarcode))
                .andExpect(jsonPath("$.responseType").value("ERROR"));
    }
}