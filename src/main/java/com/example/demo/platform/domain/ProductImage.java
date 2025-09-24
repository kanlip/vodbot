package com.example.demo.platform.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    private UUID id;
    private UUID productId;
    private String imageUrl;
    private String altText;
    private Integer sortOrder;
    private boolean primary;
    private Instant createdAt;

    public void markAsPrimary() {
        this.primary = true;
    }

    public void markAsSecondary() {
        this.primary = false;
    }
}