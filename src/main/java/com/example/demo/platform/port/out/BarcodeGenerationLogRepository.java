package com.example.demo.platform.port.out;

import com.example.demo.platform.domain.BarcodeGenerationLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BarcodeGenerationLogRepository {
    BarcodeGenerationLog save(BarcodeGenerationLog log);
    Optional<BarcodeGenerationLog> findById(UUID id);
    List<BarcodeGenerationLog> findByProductId(UUID productId);
    List<BarcodeGenerationLog> findByProductIdOrderByCreatedAtDesc(UUID productId);
    List<BarcodeGenerationLog> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);
    void deleteById(UUID id);
}