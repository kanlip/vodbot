package com.example.demo.order.adapter.out.persistence;

import com.example.demo.shared.domain.Platform;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.TenantId;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
@Table(name = "orders", uniqueConstraints = @UniqueConstraint(name = "uq_seller_platform_order", columnNames = {"seller_id", "platform_order_id"}))
public class OrderEntity {
    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "seller_id", nullable = false, length = 100)
    @TenantId
    private String sellerId;

    @Column(name = "platform_order_id", nullable = false, length = 120)
    private String platformOrderId;

    @Column(name = "order_date")
    private Instant orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 40)
    private Platform platform;

    @Column(name = "status", length = 40)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PackageEntity> packages = new ArrayList<>();

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
