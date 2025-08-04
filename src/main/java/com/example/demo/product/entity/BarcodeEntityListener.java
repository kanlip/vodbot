package com.example.demo.product.entity;


import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class BarcodeEntityListener extends AbstractMongoEventListener<BarcodeEntity> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<BarcodeEntity> event) {
        BarcodeEntity entity = event.getSource();
        
        // Auto-generate barcode value if not provided
        if (entity.getBarcodeValue() == null || entity.getBarcodeValue().isEmpty()) {
            // Example: system-generated barcode
            entity.setBarcodeValue("SYS_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        }
        
        // Set default type if not provided
        if (entity.getType() == null || entity.getType().isEmpty()) {
            entity.setType("system_generated");
        }
        
        // Set default status if not provided
        if (entity.getStatus() == null || entity.getStatus().isEmpty()) {
            entity.setStatus("active");
        }
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<BarcodeEntity> event) {
        BarcodeEntity entity = event.getSource();
        Instant now = Instant.now();
        
        // Set createdAt if this is a new entity
        if (entity.getId() == null && entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        
        // Always update updatedAt
        entity.setUpdatedAt(now);
    }
}
