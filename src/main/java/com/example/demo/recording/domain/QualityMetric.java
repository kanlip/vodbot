package com.example.demo.recording.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityMetric {
    private UUID id;
    private UUID sessionId;
    private MetricType metricType;
    private BigDecimal score; // 0.00 to 1.00
    private Map<String, Object> details;
    private Instant measuredAt;

    public enum MetricType {
        VIDEO_CLARITY,
        AUDIO_QUALITY,
        LIGHTING,
        STABILITY
    }

    public boolean isGoodQuality() {
        return score != null && score.compareTo(BigDecimal.valueOf(0.7)) >= 0;
    }

    public boolean isPoorQuality() {
        return score != null && score.compareTo(BigDecimal.valueOf(0.4)) < 0;
    }

    public String getQualityLevel() {
        if (score == null) return "UNKNOWN";

        if (score.compareTo(BigDecimal.valueOf(0.8)) >= 0) {
            return "EXCELLENT";
        } else if (score.compareTo(BigDecimal.valueOf(0.6)) >= 0) {
            return "GOOD";
        } else if (score.compareTo(BigDecimal.valueOf(0.4)) >= 0) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }
}