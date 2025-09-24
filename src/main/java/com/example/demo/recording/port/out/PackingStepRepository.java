package com.example.demo.recording.port.out;

import com.example.demo.recording.domain.PackingStep;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PackingStepRepository {
    PackingStep save(PackingStep step);
    Optional<PackingStep> findById(UUID id);
    List<PackingStep> findByOrgId(UUID orgId);
    List<PackingStep> findActiveByOrgId(UUID orgId);
    List<PackingStep> findByOrgIdOrderBySortOrder(UUID orgId);
    void deleteById(UUID id);
    boolean existsByOrgIdAndStepName(UUID orgId, String stepName);
}