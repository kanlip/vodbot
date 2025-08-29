package com.example.demo.users.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Getter
@Setter
public class UsersEntity {
    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", length = 200)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", length = 60)
    private List<String> roles = new ArrayList<>();

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(name = "is_supervisor")
    private Boolean isSupervisor;

    @Column(name = "supervisor_pin_hash", length = 200)
    private String supervisorPinHash;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "status", length = 40)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "organizations_id", nullable = false)
    private OrganizationsEntity organizationsEntity;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }
}
