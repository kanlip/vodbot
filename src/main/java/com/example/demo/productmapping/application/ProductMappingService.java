package com.example.demo.productmapping.application;

import com.example.demo.productmapping.domain.ProductMapping;
import com.example.demo.productmapping.port.in.ProductMappingUseCase;
import com.example.demo.productmapping.port.out.ProductMappingRepository;
import com.example.demo.shared.domain.Platform;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for Product Mapping operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
@Transactional
public class ProductMappingService implements ProductMappingUseCase {
    
    private final ProductMappingRepository repository;
    
    @Override
    public ProductMapping createMapping(ProductMapping productMapping) {
        log.info("Creating product mapping for seller: {}, platform: {}", 
                productMapping.getSellerId(), productMapping.getPlatform());
        
        validateMappingForCreation(productMapping);
        
        // Generate SKU and barcode if missing
        productMapping.generateSkuIfMissing();
        productMapping.generateBarcodeIfMissing();
        
        // Set timestamps and defaults (don't set ID - let JPA handle it)
        productMapping.setCreatedAt(Instant.now());
        productMapping.setUpdatedAt(Instant.now());
        productMapping.setActive(true);
        
        return repository.save(productMapping);
    }
    
    @Override
    public ProductMapping updateMapping(UUID id, ProductMapping productMapping) {
        log.info("Updating product mapping with id: {}", id);
        
        ProductMapping existing = findById(id);
        validateMappingForUpdate(existing, productMapping);
        
        // Update fields
        existing.setSku(productMapping.getSku());
        existing.setBarcode(productMapping.getBarcode());
        existing.setPlatformAlias(productMapping.getPlatformAlias());
        existing.setErpCode(productMapping.getErpCode());
        existing.setProductName(productMapping.getProductName());
        existing.setDescription(productMapping.getDescription());
        existing.setActive(productMapping.isActive());
        existing.setUpdatedAt(Instant.now());
        
        return repository.save(existing);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductMapping findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product mapping not found with id: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductMapping> findAll() {
        return repository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductMapping> findBySellerId(String sellerId) {
        return repository.findBySellerId(sellerId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductMapping> findBySellerIdAndPlatform(String sellerId, Platform platform) {
        return repository.findBySellerIdAndPlatform(sellerId, platform);
    }
    
    @Override
    public void deleteMapping(UUID id) {
        log.info("Deleting product mapping with id: {}", id);
        repository.deleteById(id);
    }
    
    @Override
    public void deactivateMapping(UUID id) {
        log.info("Deactivating product mapping with id: {}", id);
        ProductMapping mapping = findById(id);
        mapping.setActive(false);
        mapping.setUpdatedAt(Instant.now());
        repository.save(mapping);
    }
    
    @Override
    public ProductMapping syncFromPlatform(String sellerId, Platform platform, String platformProductId, String productName) {
        log.info("Syncing product from platform: {} for seller: {}", platform, sellerId);
        
        // Check if mapping already exists
        return repository.findBySellerIdAndPlatformAndPlatformProductId(sellerId, platform, platformProductId)
                .orElseGet(() -> {
                    // Create new mapping
                    ProductMapping newMapping = ProductMapping.builder()
                            .sellerId(sellerId)
                            .platform(platform)
                            .platformProductId(platformProductId)
                            .productName(productName)
                            .build();
                    
                    return createMapping(newMapping);
                });
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductMapping> findConflictingMappings(String sellerId, String sku, String barcode) {
        List<ProductMapping> conflicts = new ArrayList<>();
        
        if (sku != null && !sku.trim().isEmpty()) {
            repository.findBySellerIdAndSku(sellerId, sku).ifPresent(conflicts::add);
        }
        
        if (barcode != null && !barcode.trim().isEmpty()) {
            repository.findBySellerIdAndBarcode(sellerId, barcode).ifPresent(conflicts::add);
        }
        
        return conflicts;
    }
    
    private void validateMappingForCreation(ProductMapping productMapping) {
        // Check for duplicate SKU
        if (productMapping.getSku() != null && !productMapping.getSku().trim().isEmpty()) {
            repository.findBySellerIdAndSku(productMapping.getSellerId(), productMapping.getSku())
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("SKU already exists: " + productMapping.getSku());
                    });
        }
        
        // Check for duplicate barcode
        if (productMapping.getBarcode() != null && !productMapping.getBarcode().trim().isEmpty()) {
            repository.findBySellerIdAndBarcode(productMapping.getSellerId(), productMapping.getBarcode())
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Barcode already exists: " + productMapping.getBarcode());
                    });
        }
        
        // Check for duplicate platform product ID
        repository.findBySellerIdAndPlatformAndPlatformProductId(
                productMapping.getSellerId(), 
                productMapping.getPlatform(), 
                productMapping.getPlatformProductId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Platform product ID already exists: " + productMapping.getPlatformProductId());
                });
    }
    
    private void validateMappingForUpdate(ProductMapping existing, ProductMapping updated) {
        // Check SKU conflicts (excluding current mapping)
        if (!existing.getSku().equals(updated.getSku())) {
            repository.findBySellerIdAndSku(existing.getSellerId(), updated.getSku())
                    .filter(mapping -> !mapping.getId().equals(existing.getId()))
                    .ifPresent(conflicting -> {
                        throw new IllegalArgumentException("SKU already exists: " + updated.getSku());
                    });
        }
        
        // Check barcode conflicts (excluding current mapping)
        if (!existing.getBarcode().equals(updated.getBarcode())) {
            repository.findBySellerIdAndBarcode(existing.getSellerId(), updated.getBarcode())
                    .filter(mapping -> !mapping.getId().equals(existing.getId()))
                    .ifPresent(conflicting -> {
                        throw new IllegalArgumentException("Barcode already exists: " + updated.getBarcode());
                    });
        }
    }
}