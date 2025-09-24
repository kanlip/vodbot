package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
    List<ProductEntity> findByOrgId(UUID orgId);
    List<ProductEntity> findByOrgIdAndStatus(UUID orgId, Product.ProductStatus status);
    Optional<ProductEntity> findByOrgIdAndMasterSku(UUID orgId, String masterSku);
    Optional<ProductEntity> findByBarcode(String barcode);
    List<ProductEntity> findByOrgIdAndCategory(UUID orgId, String category);
    List<ProductEntity> findByOrgIdAndBrand(UUID orgId, String brand);

    @Query("SELECT p FROM ProductEntity p WHERE p.orgId = :orgId AND (p.barcode IS NULL OR p.barcode = '') AND p.status = 'ACTIVE'")
    List<ProductEntity> findProductsWithoutBarcodes(@Param("orgId") UUID orgId);
}