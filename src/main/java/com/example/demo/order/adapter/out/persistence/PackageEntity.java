package com.example.demo.order.adapter.out.persistence;

import jakarta.persistence.*;
import java.util.UUID;

import lombok.*;

@Entity
@Table(name = "packages")
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class PackageEntity {
    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "package_code", length = 120, nullable = false, unique = true)
    private String packageId; // renamed from package_id

    @Column(name = "packer_id", columnDefinition = "uuid")
    private UUID packerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_package_order"))
    private OrderEntity order;

    @PrePersist
    void prePersist() { if (id == null) id = UUID.randomUUID(); }
}
