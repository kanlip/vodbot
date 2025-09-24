package com.example.demo.recording.port.in;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CreatePackingStepCommand {
    private final UUID orgId;
    private final String stepName;
    private final String description;
    private final boolean required;
    private final Integer expectedDurationSeconds;
    private final Integer sortOrder;
}