package com.example.demo.barcode;

import com.example.demo.common.IS3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BarcodeController.class)
class BarcodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IS3Service s3Service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnPresignedUrlForValidBarcode() throws Exception {
        // Given
        String testBarcode = "123456789";
        String mockPresignedUrl = "https://test-bucket.s3.amazonaws.com/presigned-url";
        BarcodeRequest request = new BarcodeRequest();
        request.setBarcodeValue(testBarcode);

        when(s3Service.getPresignedUriForBarcode(testBarcode)).thenReturn(mockPresignedUrl);

        // When & Then
        mockMvc.perform(post("/api/barcode/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Presigned URL generated successfully"))
                .andExpect(jsonPath("$.presignedUrl").value(mockPresignedUrl))
                .andExpect(jsonPath("$.barcodeValue").value(testBarcode));
    }

    @Test
    void shouldReturnErrorWhenS3ServiceFails() throws Exception {
        // Given
        String testBarcode = "123456789";
        BarcodeRequest request = new BarcodeRequest();
        request.setBarcodeValue(testBarcode);

        when(s3Service.getPresignedUriForBarcode(testBarcode))
                .thenThrow(new RuntimeException("S3 service error"));

        // When & Then
        mockMvc.perform(post("/api/barcode/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error generating presigned URL: S3 service error"))
                .andExpect(jsonPath("$.barcodeValue").value(testBarcode));
    }
}