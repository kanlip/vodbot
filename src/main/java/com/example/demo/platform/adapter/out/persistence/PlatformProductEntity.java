package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.PlatformProduct;
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
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "platform_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformProductEntity {
    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "platform_integration_id", nullable = false)
    private UUID platformIntegrationId;

    @Column(name = "platform_product_id", nullable = false)
    private String platformProductId;

    @Column(name = "platform_sku")
    private String platformSku;

    @Column(name = "platform_name")
    private String platformName;

    @Column(name = "platform_price", precision = 10, scale = 2)
    private BigDecimal platformPrice;

    @Column(name = "currency")
    private String currency;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "platform_status")
    private String platformStatus;

    @Column(name = "platform_url")
    private String platformUrl;

    @Column(name = "platform_barcode")
    private String platformBarcode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "platform_data", columnDefinition = "jsonb")
    private Map<String, Object> platformData;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status")
    private PlatformProduct.SyncStatus syncStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}