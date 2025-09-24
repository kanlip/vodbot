package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.PlatformIntegration;
import com.example.demo.platform.port.out.PlatformIntegrationRepository;
import com.example.demo.shared.domain.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlatformIntegrationRepositoryImpl implements PlatformIntegrationRepository {

    private final PlatformIntegrationJpaRepository jpaRepository;
    private final PlatformIntegrationMapper mapper;

    @Override
    public PlatformIntegration save(PlatformIntegration integration) {
        PlatformIntegrationEntity entity = mapper.toEntity(integration);
        PlatformIntegrationEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PlatformIntegration> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<PlatformIntegration> findByOAuthState(String state) {
        return jpaRepository.findByOauthState(state)
                .map(mapper::toDomain);
    }

    @Override
    public List<PlatformIntegration> findByOrgId(UUID orgId) {
        return jpaRepository.findByOrgId(orgId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<PlatformIntegration> findAuthorizedByOrgId(UUID orgId) {
        return jpaRepository.findByOrgIdAndAuthorizationStatus(orgId, PlatformIntegration.AuthorizationStatus.AUTHORIZED)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<PlatformIntegration> findByOrgIdAndPlatform(UUID orgId, Platform platform) {
        return jpaRepository.findByOrgIdAndPlatform(orgId, platform)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<PlatformIntegration> findByOrgIdAndPlatformAndSellerId(UUID orgId, Platform platform, String sellerId) {
        return jpaRepository.findByOrgIdAndPlatformAndSellerId(orgId, platform, sellerId)
                .map(mapper::toDomain);
    }

    @Override
    public List<PlatformIntegration> findTokensNeedingRefresh() {
        // Find tokens that expire within 5 minutes
        Instant threshold = Instant.now().plusSeconds(300);
        return jpaRepository.findTokensNeedingRefresh(threshold)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}