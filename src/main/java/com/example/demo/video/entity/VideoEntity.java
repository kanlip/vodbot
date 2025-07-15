package com.example.demo.video.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.Instant;
import java.util.List;

import com.example.demo.user.entity.CompanyEntity;
import com.example.demo.product.entity.BarcodeEntity;

@Document(collection = "videos")
@Getter
@Setter
public class VideoEntity {
    @Id
    private String id;

    @DocumentReference
    private CompanyEntity company;
    private ObjectId companyId;
    private ObjectId orderId;
    private String platformOrderId;
    private ObjectId recordedByUserId;
    private String packerName;
    private String s3Key;
    private String s3Bucket;
    private String videoUrl;
    private Instant recordedAt;
    private Integer durationSeconds;
    private Double fileSizeMB;
    private String resolution;
    private String status;
    private String notes;
    private List<ItemScan> itemScans;
    private String disputeStatus;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    public static class ItemScan {
        private Integer timestampOffsetSeconds;
        private String sku;
        private Integer quantity;
        private String status;
        @DocumentReference
        private BarcodeEntity barcodeEntity; // Reference to BarcodeEntity
        private String barcodeEntityId; // Optionally keep the id for denormalized access
        // Optionally, you can add more fields from BarcodeEntity if you want to denormalize further
    }
    // getters and setters
}
