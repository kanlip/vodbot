package com.example.demo.productmapping.adapter.out.persistence;

import com.example.demo.shared.domain.Platform;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for product mapping.
 */
@Entity
@Table(name = "product_mappings", indexes = {
    @Index(name = "idx_seller_id", columnList = "seller_id"),
    @Index(name = "idx_seller_platform", columnList = "seller_id, platform"),
    @Index(name = "idx_sku", columnList = "seller_id, sku"),
    @Index(name = "idx_barcode", columnList = "seller_id, barcode"),
    @Index(name = "idx_platform_product", columnList = "seller_id, platform, platform_product_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductMappingEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 100)
    private String sku;
    
    @Column(length = 100)
    private String barcode;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;
    
    @Column(name = "platform_product_id", nullable = false, length = 100)
    private String platformProductId;
    
    @Column(name = "platform_alias", length = 255)
    private String platformAlias;
    
    @Column(name = "erp_code", length = 100)
    private String erpCode;
    
    @Column(name = "seller_id", nullable = false, length = 100)
    private String sellerId;
    
    @Column(name = "product_name", length = 500)
    private String productName;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}