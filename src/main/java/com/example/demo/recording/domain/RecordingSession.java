package com.example.demo.recording.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingSession {
    private UUID id;
    private UUID orderId;
    private UUID packerId;
    private SessionType sessionType;
    private SessionStatus status;
    private Instant startedAt;
    private Instant endedAt;
    private Integer durationSeconds;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;

    private List<VideoFile> videoFiles;
    private List<QualityMetric> qualityMetrics;
    private List<StepCompletion> stepCompletions;

    public enum SessionType {
        PACKING,
        QUALITY_CHECK,
        CUSTOM
    }

    public enum SessionStatus {
        STARTED,
        RECORDING,
        PAUSED,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    public void startSession() {
        this.status = SessionStatus.RECORDING;
        this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void endSession() {
        this.endedAt = Instant.now();
        this.status = SessionStatus.PROCESSING;
        this.updatedAt = Instant.now();

        if (this.startedAt != null) {
            this.durationSeconds = (int) (this.endedAt.getEpochSecond() - this.startedAt.getEpochSecond());
        }
    }

    public void markCompleted() {
        this.status = SessionStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void markFailed() {
        this.status = SessionStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public boolean isInProgress() {
        return status == SessionStatus.RECORDING || status == SessionStatus.PROCESSING;
    }

    public void updateStatus(SessionStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void pauseSession() {
        this.status = SessionStatus.PAUSED;
        this.updatedAt = Instant.now();
    }

    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED;
    }
}