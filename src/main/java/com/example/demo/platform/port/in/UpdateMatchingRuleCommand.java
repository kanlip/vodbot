package com.example.demo.platform.port.in;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class UpdateMatchingRuleCommand {
    private final String ruleName;
    private final List<String> matchFields;
    private final BigDecimal similarityThreshold;
    private final Integer priority;
    private final Boolean active;
}