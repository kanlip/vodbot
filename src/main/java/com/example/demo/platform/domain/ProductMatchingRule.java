package com.example.demo.platform.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductMatchingRule {
    private UUID id;
    private UUID orgId;
    private String ruleName;
    private List<String> matchFields; // ["name", "sku", "attributes.color"]
    private BigDecimal similarityThreshold; // 0.00 to 1.00
    private boolean active;
    private Integer priority; // Lower number = higher priority
    private Instant createdAt;

    public boolean isActive() {
        return active;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean hasHighPriority() {
        return priority != null && priority <= 5;
    }
}