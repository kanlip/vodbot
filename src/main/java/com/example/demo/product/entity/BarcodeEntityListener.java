package com.example.demo.product.entity;


import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BarcodeEntityListener extends AbstractMongoEventListener<BarcodeEntity> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<BarcodeEntity> event) {
        BarcodeEntity entity = event.getSource();
        if (entity.getBarcodeValue() == null || entity.getBarcodeValue().isEmpty()) {
            // Example: system-generated barcode
            entity.setBarcodeValue("SYS_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        }
    }
}
