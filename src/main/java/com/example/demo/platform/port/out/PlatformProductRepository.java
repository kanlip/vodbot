package com.example.demo.platform.port.out;

import com.example.demo.platform.domain.PlatformProduct;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlatformProductRepository {
    PlatformProduct save(PlatformProduct platformProduct);
    Optional<PlatformProduct> findById(UUID id);
    List<PlatformProduct> findByProductId(UUID productId);
    List<PlatformProduct> findByPlatformIntegrationId(UUID integrationId);
    Optional<PlatformProduct> findByIntegrationIdAndPlatformProductId(UUID integrationId, String platformProductId);
    List<PlatformProduct> findByIntegrationIdAndSyncStatus(UUID integrationId, PlatformProduct.SyncStatus syncStatus);
    void deleteById(UUID id);
    void deleteByProductId(UUID productId);
}