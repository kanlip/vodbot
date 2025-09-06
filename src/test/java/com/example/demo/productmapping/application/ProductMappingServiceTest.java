package com.example.demo.productmapping.application;

import com.example.demo.productmapping.domain.ProductMapping;
import com.example.demo.productmapping.port.out.ProductMappingRepository;
import com.example.demo.shared.domain.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProductMappingServiceTest {
    
    @Mock
    private ProductMappingRepository repository;
    
    private ProductMappingService service;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ProductMappingService(repository);
    }
    
    @Test
    void createMapping_ShouldGenerateSkuAndBarcodeWhenMissing() {
        // Given
        ProductMapping mapping = ProductMapping.builder()
                .sellerId("seller123")
                .platform(Platform.SHOPEE)
                .platformProductId("prod123")
                .productName("Test Product")
                .build();
        
        when(repository.findBySellerIdAndPlatformAndPlatformProductId(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        ProductMapping result = service.createMapping(mapping);
        
        // Then
        assertThat(result.getSku()).isNotNull().isNotEmpty();
        assertThat(result.getBarcode()).isNotNull().isNotEmpty();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(result.isActive()).isTrue();
    }
    
    @Test
    void createMapping_ShouldThrowException_WhenDuplicatePlatformProductId() {
        // Given
        ProductMapping existing = ProductMapping.builder()
                .id(UUID.randomUUID())
                .sellerId("seller123")
                .platform(Platform.SHOPEE)
                .platformProductId("prod123")
                .build();
        
        ProductMapping newMapping = ProductMapping.builder()
                .sellerId("seller123")
                .platform(Platform.SHOPEE)
                .platformProductId("prod123")
                .productName("Test Product")
                .build();
        
        when(repository.findBySellerIdAndPlatformAndPlatformProductId("seller123", Platform.SHOPEE, "prod123"))
                .thenReturn(Optional.of(existing));
        
        // When & Then
        assertThatThrownBy(() -> service.createMapping(newMapping))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Platform product ID already exists");
    }
    
    @Test
    void syncFromPlatform_ShouldReturnExisting_WhenMappingExists() {
        // Given
        ProductMapping existing = ProductMapping.builder()
                .id(UUID.randomUUID())
                .sellerId("seller123")
                .platform(Platform.LAZADA)
                .platformProductId("prod456")
                .productName("Existing Product")
                .build();
        
        when(repository.findBySellerIdAndPlatformAndPlatformProductId("seller123", Platform.LAZADA, "prod456"))
                .thenReturn(Optional.of(existing));
        
        // When
        ProductMapping result = service.syncFromPlatform("seller123", Platform.LAZADA, "prod456", "New Name");
        
        // Then
        assertThat(result).isEqualTo(existing);
    }
    
    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product mapping not found with id");
    }
}