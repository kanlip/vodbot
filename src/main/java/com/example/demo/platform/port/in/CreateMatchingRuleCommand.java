package com.example.demo.platform.port.in;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CreateMatchingRuleCommand {
    private final UUID orgId;
    private final String ruleName;
    private final List<String> matchFields;
    private final BigDecimal similarityThreshold;
    private final Integer priority;
}