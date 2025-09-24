package com.example.demo.recording.port.out;

import com.example.demo.recording.domain.StepCompletion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StepCompletionRepository {
    StepCompletion save(StepCompletion completion);
    Optional<StepCompletion> findById(UUID id);
    List<StepCompletion> findBySessionId(UUID sessionId);
    List<StepCompletion> findBySessionIdOrderByStepOrder(UUID sessionId);
    Optional<StepCompletion> findBySessionIdAndStepId(UUID sessionId, UUID stepId);
    void deleteById(UUID id);
    void deleteBySessionId(UUID sessionId);
}