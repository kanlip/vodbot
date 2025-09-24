package com.example.demo.platform.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product_matching_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMatchingRuleEntity {
    @Id
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "match_fields", nullable = false, columnDefinition = "jsonb")
    private List<String> matchFields;

    @Column(name = "similarity_threshold", precision = 3, scale = 2)
    private BigDecimal similarityThreshold;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}