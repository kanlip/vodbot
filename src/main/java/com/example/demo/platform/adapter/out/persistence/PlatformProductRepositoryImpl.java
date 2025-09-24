package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.PlatformProduct;
import com.example.demo.platform.port.out.PlatformProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlatformProductRepositoryImpl implements PlatformProductRepository {

    private final PlatformProductJpaRepository jpaRepository;
    private final PlatformProductMapper mapper;

    @Override
    public PlatformProduct save(PlatformProduct platformProduct) {
        PlatformProductEntity entity = mapper.toEntity(platformProduct);
        PlatformProductEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PlatformProduct> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<PlatformProduct> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<PlatformProduct> findByPlatformIntegrationId(UUID integrationId) {
        return jpaRepository.findByPlatformIntegrationId(integrationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<PlatformProduct> findByIntegrationIdAndPlatformProductId(UUID integrationId, String platformProductId) {
        return jpaRepository.findByPlatformIntegrationIdAndPlatformProductId(integrationId, platformProductId)
                .map(mapper::toDomain);
    }

    @Override
    public List<PlatformProduct> findByIntegrationIdAndSyncStatus(UUID integrationId, PlatformProduct.SyncStatus syncStatus) {
        return jpaRepository.findByPlatformIntegrationIdAndSyncStatus(integrationId, syncStatus)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByProductId(UUID productId) {
        jpaRepository.deleteByProductId(productId);
    }
}