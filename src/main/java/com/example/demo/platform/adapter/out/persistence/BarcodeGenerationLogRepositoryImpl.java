package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.BarcodeGenerationLog;
import com.example.demo.platform.port.out.BarcodeGenerationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BarcodeGenerationLogRepositoryImpl implements BarcodeGenerationLogRepository {

    private final BarcodeGenerationLogJpaRepository jpaRepository;
    private final BarcodeGenerationLogMapper mapper;

    @Override
    public BarcodeGenerationLog save(BarcodeGenerationLog log) {
        BarcodeGenerationLogEntity entity = mapper.toEntity(log);
        BarcodeGenerationLogEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<BarcodeGenerationLog> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<BarcodeGenerationLog> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<BarcodeGenerationLog> findByProductIdOrderByCreatedAtDesc(UUID productId) {
        return jpaRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<BarcodeGenerationLog> findByBarcode(String barcode) {
        return jpaRepository.findByBarcode(barcode)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByBarcode(String barcode) {
        return jpaRepository.existsByBarcode(barcode);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}