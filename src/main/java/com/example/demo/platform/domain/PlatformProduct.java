package com.example.demo.platform.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformProduct {
    private UUID id;
    private UUID productId;
    private UUID platformIntegrationId;
    private String platformProductId;
    private String platformSku;
    private String platformName;
    private BigDecimal platformPrice;
    private String currency;
    private Integer stockQuantity;
    private String platformStatus;
    private String platformUrl;
    private String platformBarcode; // Platform's own barcode/SKU if exists
    private Map<String, Object> platformData;
    private Instant lastSyncedAt;
    private SyncStatus syncStatus;
    private Instant createdAt;
    private Instant updatedAt;

    public enum SyncStatus {
        SUCCESS,
        FAILED,
        PENDING
    }

    public boolean needsSync() {
        return lastSyncedAt == null ||
               syncStatus == SyncStatus.FAILED ||
               syncStatus == SyncStatus.PENDING;
    }

    public void markSyncSuccess() {
        this.syncStatus = SyncStatus.SUCCESS;
        this.lastSyncedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markSyncFailed() {
        this.syncStatus = SyncStatus.FAILED;
        this.lastSyncedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateFromPlatform(String platformName, BigDecimal price, Integer stock, String status, Map<String, Object> data) {
        this.platformName = platformName;
        this.platformPrice = price;
        this.stockQuantity = stock;
        this.platformStatus = status;
        this.platformData = data;
        this.updatedAt = Instant.now();
        markSyncSuccess();
    }
}