package com.example.demo.users.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "organizations", indexes = {
        @Index(name = "idx_org_name", columnList = "name", unique = true)
})
@Getter
@Setter
public class OrganizationsEntity {
    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @TenantId
    private String tenantId;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 40)
    private String status;

    @Embedded
    private Plan plan;

    @Embedded
    private Billing billing;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = UsersEntity_.ORGANIZATIONS_ENTITY, orphanRemoval = true)
    private Set<UsersEntity> usersEntities = new LinkedHashSet<>();

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    @Embeddable
    @Getter
    @Setter
    public static class Plan {
        @Column(name = "plan_code", length = 60)
        private String code;
        @Column(name = "plan_started_at")
        private Instant startedAt;
        @Column(name = "plan_expires_at")
        private Instant expiresAt;
    }

    @Embeddable
    @Getter
    @Setter
    public static class Billing {
        @Column(name = "billing_contact_email", length = 150)
        private String contactEmail;
        @Column(name = "billing_cycle", length = 40)
        private String billingCycle;
        @Column(name = "billing_currency", length = 10)
        private String currency;
    }
}
