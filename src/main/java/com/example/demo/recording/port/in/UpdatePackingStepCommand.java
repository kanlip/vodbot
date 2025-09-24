package com.example.demo.recording.port.in;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdatePackingStepCommand {
    private final String stepName;
    private final String description;
    private final Boolean required;
    private final Integer expectedDurationSeconds;
    private final Integer sortOrder;
    private final Boolean active;
}