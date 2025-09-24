package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.PlatformProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformProductJpaRepository extends JpaRepository<PlatformProductEntity, UUID> {
    List<PlatformProductEntity> findByProductId(UUID productId);
    List<PlatformProductEntity> findByPlatformIntegrationId(UUID integrationId);
    Optional<PlatformProductEntity> findByPlatformIntegrationIdAndPlatformProductId(UUID integrationId, String platformProductId);
    List<PlatformProductEntity> findByPlatformIntegrationIdAndSyncStatus(UUID integrationId, PlatformProduct.SyncStatus syncStatus);
    void deleteByProductId(UUID productId);
}