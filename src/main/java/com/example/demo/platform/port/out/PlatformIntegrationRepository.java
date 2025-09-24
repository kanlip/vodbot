package com.example.demo.platform.port.out;

import com.example.demo.platform.domain.PlatformIntegration;
import com.example.demo.shared.domain.Platform;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlatformIntegrationRepository {
    PlatformIntegration save(PlatformIntegration integration);
    Optional<PlatformIntegration> findById(UUID id);
    Optional<PlatformIntegration> findByOAuthState(String state);
    List<PlatformIntegration> findByOrgId(UUID orgId);
    List<PlatformIntegration> findAuthorizedByOrgId(UUID orgId);
    List<PlatformIntegration> findByOrgIdAndPlatform(UUID orgId, Platform platform);
    Optional<PlatformIntegration> findByOrgIdAndPlatformAndSellerId(UUID orgId, Platform platform, String sellerId);
    List<PlatformIntegration> findTokensNeedingRefresh();
    void deleteById(UUID id);
}