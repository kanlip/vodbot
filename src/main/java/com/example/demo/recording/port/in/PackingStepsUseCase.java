package com.example.demo.recording.port.in;

import com.example.demo.recording.domain.PackingStep;
import com.example.demo.recording.domain.StepCompletion;

import java.util.List;
import java.util.UUID;

public interface PackingStepsUseCase {
    PackingStep createStep(CreatePackingStepCommand command);
    PackingStep updateStep(UUID stepId, UpdatePackingStepCommand command);
    void deleteStep(UUID stepId);
    List<PackingStep> getOrganizationSteps(UUID orgId);
    PackingStep getStep(UUID stepId);

    StepCompletion startStep(UUID sessionId, UUID stepId, Integer videoTimestamp);
    StepCompletion completeStep(UUID completionId, String notes, Integer videoTimestamp);
    StepCompletion skipStep(UUID completionId, String reason);
    List<StepCompletion> getSessionStepCompletions(UUID sessionId);
}