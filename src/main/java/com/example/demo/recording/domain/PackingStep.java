package com.example.demo.recording.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackingStep {
    private UUID id;
    private UUID orgId;
    private String stepName;
    private String description;
    private boolean required;
    private Integer expectedDurationSeconds;
    private Integer sortOrder;
    private boolean active;
    private Instant createdAt;

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}