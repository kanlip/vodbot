package com.example.demo.productmapping.adapter.in.web;

import com.example.demo.productmapping.domain.ProductMapping;
import com.example.demo.productmapping.port.in.ProductMappingUseCase;
import com.example.demo.shared.domain.Platform;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductMappingController.class)
class ProductMappingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ProductMappingUseCase productMappingUseCase;
    
    @Test
    void getAllMappings_ShouldReturnMappings() throws Exception {
        // Given
        ProductMapping mapping = ProductMapping.builder()
                .id(UUID.randomUUID())
                .sellerId("seller123")
                .platform(Platform.SHOPEE)
                .sku("SKU123")
                .barcode("BC123")
                .platformProductId("prod123")
                .productName("Test Product")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        when(productMappingUseCase.findAll()).thenReturn(List.of(mapping));
        
        // When & Then
        mockMvc.perform(get("/api/product-mappings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].sellerId").value("seller123"))
                .andExpect(jsonPath("$[0].platform").value("SHOPEE"));
    }
    
    @Test
    void createMapping_ShouldReturnCreatedMapping() throws Exception {
        // Given
        ProductMapping inputMapping = ProductMapping.builder()
                .sellerId("seller123")
                .platform(Platform.TIKTOK)
                .platformProductId("prod789")
                .productName("New Product")
                .sku("TIKTOK-123") // Add valid SKU
                .build();
        
        ProductMapping createdMapping = ProductMapping.builder()
                .id(UUID.randomUUID())
                .sellerId("seller123")
                .platform(Platform.TIKTOK)
                .sku("TIKTOK-123456789")
                .barcode("BC987654321")
                .platformProductId("prod789")
                .productName("New Product")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        when(productMappingUseCase.createMapping(any())).thenReturn(createdMapping);
        
        // When & Then
        mockMvc.perform(post("/api/product-mappings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputMapping)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.sku").exists())
                .andExpect(jsonPath("$.barcode").exists());
    }
    
    @Test
    void getMappingsBySeller_ShouldReturnSellerMappings() throws Exception {
        // Given
        String sellerId = "seller456";
        ProductMapping mapping = ProductMapping.builder()
                .id(UUID.randomUUID())
                .sellerId(sellerId)
                .platform(Platform.LAZADA)
                .sku("LAZ-123")
                .platformProductId("prod456")
                .active(true)
                .build();
        
        when(productMappingUseCase.findBySellerId(sellerId)).thenReturn(List.of(mapping));
        
        // When & Then
        mockMvc.perform(get("/api/product-mappings/seller/" + sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sellerId").value(sellerId))
                .andExpect(jsonPath("$[0].platform").value("LAZADA"));
    }
}