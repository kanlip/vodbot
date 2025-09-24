package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.PlatformIntegration;
import com.example.demo.shared.domain.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformIntegrationJpaRepository extends JpaRepository<PlatformIntegrationEntity, UUID> {
    Optional<PlatformIntegrationEntity> findByOauthState(String oauthState);
    List<PlatformIntegrationEntity> findByOrgId(UUID orgId);
    List<PlatformIntegrationEntity> findByOrgIdAndAuthorizationStatus(UUID orgId, PlatformIntegration.AuthorizationStatus status);
    List<PlatformIntegrationEntity> findByOrgIdAndPlatform(UUID orgId, Platform platform);
    Optional<PlatformIntegrationEntity> findByOrgIdAndPlatformAndSellerId(UUID orgId, Platform platform, String sellerId);

    @Query("SELECT p FROM PlatformIntegrationEntity p WHERE p.tokenExpiresAt IS NOT NULL AND p.tokenExpiresAt <= :threshold AND p.refreshToken IS NOT NULL")
    List<PlatformIntegrationEntity> findTokensNeedingRefresh(@Param("threshold") Instant threshold);
}