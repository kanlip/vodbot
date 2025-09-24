package com.example.demo.platform.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductMatchingRuleJpaRepository extends JpaRepository<ProductMatchingRuleEntity, UUID> {
    List<ProductMatchingRuleEntity> findByOrgId(UUID orgId);
    List<ProductMatchingRuleEntity> findByOrgIdAndActive(UUID orgId, boolean active);
    List<ProductMatchingRuleEntity> findByOrgIdOrderByPriority(UUID orgId);
    boolean existsByOrgIdAndRuleName(UUID orgId, String ruleName);
}