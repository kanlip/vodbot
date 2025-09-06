package com.example.demo.productmapping.port.in;

import com.example.demo.productmapping.domain.ProductMapping;
import com.example.demo.shared.domain.Platform;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

/**
 * Use case interface for Product Mapping operations.
 */
public interface ProductMappingUseCase {
    
    ProductMapping createMapping(@Valid ProductMapping productMapping);
    
    ProductMapping updateMapping(UUID id, @Valid ProductMapping productMapping);
    
    ProductMapping findById(UUID id);
    
    List<ProductMapping> findAll();
    
    List<ProductMapping> findBySellerId(String sellerId);
    
    List<ProductMapping> findBySellerIdAndPlatform(String sellerId, Platform platform);
    
    void deleteMapping(UUID id);
    
    void deactivateMapping(UUID id);
    
    ProductMapping syncFromPlatform(String sellerId, Platform platform, String platformProductId, String productName);
    
    List<ProductMapping> findConflictingMappings(String sellerId, String sku, String barcode);
}