package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.ProductMatchingRule;
import com.example.demo.platform.port.out.ProductMatchingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductMatchingRuleRepositoryImpl implements ProductMatchingRuleRepository {

    private final ProductMatchingRuleJpaRepository jpaRepository;
    private final ProductMatchingRuleMapper mapper;

    @Override
    public ProductMatchingRule save(ProductMatchingRule rule) {
        ProductMatchingRuleEntity entity = mapper.toEntity(rule);
        ProductMatchingRuleEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ProductMatchingRule> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<ProductMatchingRule> findByOrgId(UUID orgId) {
        return jpaRepository.findByOrgId(orgId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ProductMatchingRule> findActiveByOrgId(UUID orgId) {
        return jpaRepository.findByOrgIdAndActive(orgId, true)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ProductMatchingRule> findByOrgIdOrderByPriority(UUID orgId) {
        return jpaRepository.findByOrgIdOrderByPriority(orgId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByOrgIdAndRuleName(UUID orgId, String ruleName) {
        return jpaRepository.existsByOrgIdAndRuleName(orgId, ruleName);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}