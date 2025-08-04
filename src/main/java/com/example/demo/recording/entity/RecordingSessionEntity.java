package com.example.demo.recording.entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

/**
 * Entity representing an active recording session that tracks scanned items
 * and provides audit trail functionality for package recording operations.
 */
@Data
@Builder
@Document(collection = "recording_sessions")
public class RecordingSessionEntity {
    
    @Id
    private ObjectId id;
    
    /**
     * Reference to the user who started this recording session
     */
    @Indexed
    private ObjectId userId;
    
    /**
     * Reference to the company this recording session belongs to
     */
    @Indexed
    private ObjectId companyId;
    
    /**
     * Package/Order ID being recorded
     */
    @Indexed
    private ObjectId orderId;
    
    /**
     * Platform order ID (e.g., Shopee order number)
     */
    private String platformOrderId;
    
    /**
     * Current status of the recording session
     */
    @Indexed
    private SessionStatus status;
    
    /**
     * When the recording session was started
     */
    private Instant startedAt;
    
    /**
     * When the recording session was completed/ended
     */
    private Instant endedAt;
    
    /**
     * List of items scanned during this recording session
     */
    @Builder.Default
    private List<ScannedItem> scannedItems = new ArrayList<>();
    
    /**
     * Additional notes or comments for this recording session
     */
    private String notes;
    
    /**
     * Reference to the final video entity created when session completes
     */
    private ObjectId videoEntityId;
    
    /**
     * Audit trail information
     */
    private Instant createdAt;
    private Instant updatedAt;
    private ObjectId lastModifiedBy;
    
    /**
     * Represents an item scanned during the recording session
     */
    @Data
    @Builder
    public static class ScannedItem {
        /**
         * Timestamp when this item was scanned (seconds from recording start)
         */
        private Integer timestampOffsetSeconds;
        
        /**
         * Reference to the barcode entity that was scanned
         */
        private ObjectId barcodeEntityId;
        
        /**
         * The actual barcode value that was scanned
         */
        private String barcodeValue;
        
        /**
         * SKU of the scanned item
         */
        private String sku;
        
        /**
         * Quantity of this item scanned
         */
        @Builder.Default
        private Integer quantity = 1;
        
        /**
         * Status of this scanned item (verified, disputed, etc.)
         */
        @Builder.Default
        private String status = "scanned";
        
        /**
         * When this item was scanned
         */
        private Instant scannedAt;
        
        /**
         * User who scanned this item (for audit trail)
         */
        private ObjectId scannedBy;
    }
    
    /**
     * Recording session status enumeration
     */
    public enum SessionStatus {
        STARTED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        ERROR
    }
}