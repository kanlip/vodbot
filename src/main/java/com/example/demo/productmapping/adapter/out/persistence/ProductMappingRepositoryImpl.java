package com.example.demo.productmapping.adapter.out.persistence;

import com.example.demo.productmapping.domain.ProductMapping;
import com.example.demo.productmapping.port.out.ProductMappingRepository;
import com.example.demo.shared.domain.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repository implementation using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class ProductMappingRepositoryImpl implements ProductMappingRepository {
    
    private final ProductMappingJpaRepository jpaRepository;
    private final ProductMappingMapper mapper;
    
    @Override
    public ProductMapping save(ProductMapping productMapping) {
        ProductMappingEntity entity = mapper.toEntity(productMapping);
        ProductMappingEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<ProductMapping> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<ProductMapping> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductMapping> findBySellerId(String sellerId) {
        return jpaRepository.findBySellerId(sellerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductMapping> findBySellerIdAndPlatform(String sellerId, Platform platform) {
        return jpaRepository.findBySellerIdAndPlatform(sellerId, platform).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<ProductMapping> findBySellerIdAndSku(String sellerId, String sku) {
        return jpaRepository.findBySellerIdAndSku(sellerId, sku)
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<ProductMapping> findBySellerIdAndBarcode(String sellerId, String barcode) {
        return jpaRepository.findBySellerIdAndBarcode(sellerId, barcode)
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<ProductMapping> findBySellerIdAndPlatformAndPlatformProductId(String sellerId, Platform platform, String platformProductId) {
        return jpaRepository.findBySellerIdAndPlatformAndPlatformProductId(sellerId, platform, platformProductId)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<ProductMapping> findBySellerIdAndActive(String sellerId, boolean active) {
        return jpaRepository.findBySellerIdAndActive(sellerId, active).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }
    
    @Override
    public boolean existsByBarcode(String barcode) {
        return jpaRepository.existsByBarcode(barcode);
    }
    
    @Override
    public boolean existsByPlatformAndPlatformProductId(Platform platform, String platformProductId) {
        return jpaRepository.existsByPlatformAndPlatformProductId(platform, platformProductId);
    }
}