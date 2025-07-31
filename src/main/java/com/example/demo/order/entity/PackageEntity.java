package com.example.demo.order.entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document
// Ground Truth that syncs with external platforms
public class PackageEntity {

    @Id
    ObjectId id;

    @Indexed
    String package_id;

    // Ensure the type is correctly referenced to UserEntity
    ObjectId packerId;

    @Data
    public static class Item {

        private String sku;
        private Integer quantity;

        // ... other fields as needed
    }
}
