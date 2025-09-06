package com.example.demo.productmapping.port.out;

import com.example.demo.productmapping.domain.ProductMapping;
import com.example.demo.shared.domain.Platform;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ProductMapping operations.
 */
public interface ProductMappingRepository {
    
    ProductMapping save(ProductMapping productMapping);
    
    Optional<ProductMapping> findById(UUID id);
    
    List<ProductMapping> findAll();
    
    List<ProductMapping> findBySellerId(String sellerId);
    
    List<ProductMapping> findBySellerIdAndPlatform(String sellerId, Platform platform);
    
    Optional<ProductMapping> findBySellerIdAndSku(String sellerId, String sku);
    
    Optional<ProductMapping> findBySellerIdAndBarcode(String sellerId, String barcode);
    
    Optional<ProductMapping> findBySellerIdAndPlatformAndPlatformProductId(String sellerId, Platform platform, String platformProductId);
    
    List<ProductMapping> findBySellerIdAndActive(String sellerId, boolean active);
    
    void deleteById(UUID id);
    
    boolean existsBySku(String sku);
    
    boolean existsByBarcode(String barcode);
    
    boolean existsByPlatformAndPlatformProductId(Platform platform, String platformProductId);
}