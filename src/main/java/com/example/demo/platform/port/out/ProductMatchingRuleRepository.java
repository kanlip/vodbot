package com.example.demo.platform.port.out;

import com.example.demo.platform.domain.ProductMatchingRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductMatchingRuleRepository {
    ProductMatchingRule save(ProductMatchingRule rule);
    Optional<ProductMatchingRule> findById(UUID id);
    List<ProductMatchingRule> findByOrgId(UUID orgId);
    List<ProductMatchingRule> findActiveByOrgId(UUID orgId);
    List<ProductMatchingRule> findByOrgIdOrderByPriority(UUID orgId);
    boolean existsByOrgIdAndRuleName(UUID orgId, String ruleName);
    void deleteById(UUID id);
}