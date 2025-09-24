package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.BarcodeGenerationLog;
import com.example.demo.platform.domain.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "barcode_generation_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarcodeGenerationLogEntity {
    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "barcode", nullable = false)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "barcode_type", nullable = false)
    private Product.BarcodeType barcodeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_method")
    private BarcodeGenerationLog.GenerationMethod generationMethod;

    @Column(name = "generated_by")
    private UUID generatedBy;

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