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
public class StepCompletion {
    private UUID id;
    private UUID sessionId;
    private UUID stepId;
    private CompletionStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private String notes;
    private Integer videoTimestampStart; // Seconds from session start
    private Integer videoTimestampEnd;

    public enum CompletionStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        SKIPPED,
        FAILED
    }

    public void startStep() {
        this.status = CompletionStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
    }

    public void completeStep(String notes, Integer videoTimestampEnd) {
        this.status = CompletionStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.notes = notes;
        this.videoTimestampEnd = videoTimestampEnd;
    }

    public void skipStep(String reason) {
        this.status = CompletionStatus.SKIPPED;
        this.completedAt = Instant.now();
        this.notes = reason;
    }

    public void failStep(String errorReason) {
        this.status = CompletionStatus.FAILED;
        this.completedAt = Instant.now();
        this.notes = errorReason;
    }

    public boolean isCompleted() {
        return status == CompletionStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return status == CompletionStatus.IN_PROGRESS;
    }

    public Integer getDurationSeconds() {
        if (startedAt != null && completedAt != null) {
            return (int) (completedAt.getEpochSecond() - startedAt.getEpochSecond());
        }
        return null;
    }
}