package com.example.demo.video.entity;

import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "videos")
@Getter
@Setter
public class VideoEntity {

    @Id
    private String id;

    private ObjectId companyId;

    @Indexed
    private ObjectId orderId;

    private String platformOrderId;
    private ObjectId recordedByUserId;
    private String s3Key;
    private String s3Bucket;

    @Indexed
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
        private ObjectId barcodeEntityId; // Optionally keep the id for denormalized access
        // Optionally, you can add more fields from BarcodeEntity if you want to denormalize further
    }
    // getters and setters
}
