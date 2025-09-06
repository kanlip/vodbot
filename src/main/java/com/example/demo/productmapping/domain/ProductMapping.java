package com.example.demo.productmapping.domain;

import com.example.demo.shared.domain.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Central product mapping entity that links SKU, barcode, platform alias, and ERP code.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductMapping {
    
    private UUID id;
    
    private String sku;  // Removed @NotBlank since it will be auto-generated
    
    private String barcode;
    
    @NotNull(message = "Platform cannot be null")
    private Platform platform;
    
    @NotBlank(message = "Platform product ID cannot be blank")
    private String platformProductId;
    
    private String platformAlias;
    
    private String erpCode;
    
    @NotBlank(message = "Seller ID cannot be blank")
    private String sellerId;
    
    private String productName;
    
    private String description;
    
    private boolean active;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    /**
     * Generates a barcode if not already set.
     * Uses a simple UUID-based approach for demonstration.
     */
    public void generateBarcodeIfMissing() {
        if (this.barcode == null || this.barcode.trim().isEmpty()) {
            this.barcode = "BC" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
    }
    
    /**
     * Generates an SKU if not already set.
     * Uses platform and timestamp for uniqueness.
     */
    public void generateSkuIfMissing() {
        if (this.sku == null || this.sku.trim().isEmpty()) {
            this.sku = platform.name() + "-" + System.currentTimeMillis();
        }
    }
}