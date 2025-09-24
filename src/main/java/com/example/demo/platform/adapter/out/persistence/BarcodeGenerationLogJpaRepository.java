package com.example.demo.platform.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BarcodeGenerationLogJpaRepository extends JpaRepository<BarcodeGenerationLogEntity, UUID> {
    List<BarcodeGenerationLogEntity> findByProductId(UUID productId);
    List<BarcodeGenerationLogEntity> findByProductIdOrderByCreatedAtDesc(UUID productId);
    List<BarcodeGenerationLogEntity> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);
}