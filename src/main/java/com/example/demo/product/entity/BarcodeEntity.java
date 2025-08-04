package com.example.demo.product.entity;

import com.example.demo.common.Platform;
import com.example.demo.user.entity.CompanyEntity;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.Instant;

@Data
@Builder

@Document(collection = "barcode_mappings")
/**
 * Represents a mapping between a product and its barcode.
 */
public class BarcodeEntity {
    @Id
    private ObjectId id;

    @Indexed
    private ObjectId company;

    @Indexed
    private String barcodeValue;
    private String type; // system_generated, user_defined, platform_sync
    @Indexed
    private String status; // active, inactive

    @Indexed
    private Platform platform; // shopee, lazada, tiktokshop, internal
    private String platformProductId;
    private String platformSkuId;

    private String productName;
    private VariantDetails variantDetails;

    private Instant createdAt;
    private Instant updatedAt;



    @Data
    @Builder
    public static class VariantDetails {
        private String color;
        private String size;
    }
}
