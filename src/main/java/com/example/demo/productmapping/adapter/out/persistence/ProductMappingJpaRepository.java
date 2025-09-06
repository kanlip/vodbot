package com.example.demo.productmapping.adapter.out.persistence;

import com.example.demo.shared.domain.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for ProductMappingEntity.
 */
@Repository
public interface ProductMappingJpaRepository extends JpaRepository<ProductMappingEntity, UUID> {
    
    List<ProductMappingEntity> findBySellerId(String sellerId);
    
    List<ProductMappingEntity> findBySellerIdAndPlatform(String sellerId, Platform platform);
    
    Optional<ProductMappingEntity> findBySellerIdAndSku(String sellerId, String sku);
    
    Optional<ProductMappingEntity> findBySellerIdAndBarcode(String sellerId, String barcode);
    
    Optional<ProductMappingEntity> findBySellerIdAndPlatformAndPlatformProductId(String sellerId, Platform platform, String platformProductId);
    
    List<ProductMappingEntity> findBySellerIdAndActive(String sellerId, Boolean active);
    
    boolean existsBySku(String sku);
    
    boolean existsByBarcode(String barcode);
    
    boolean existsByPlatformAndPlatformProductId(Platform platform, String platformProductId);
}