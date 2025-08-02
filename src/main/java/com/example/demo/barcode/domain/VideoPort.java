package com.example.demo.barcode.domain;

import java.time.Instant;
import java.util.List;

/**
 * Port interface for video-related operations in the barcode context
 */
public interface VideoPort {
    
    /**
     * Find or create a video entity for a package
     */
    VideoData findOrCreateVideoForPackage(String packageId);
    
    /**
     * Save an item scan to a video entity
     */
    void saveItemScanToVideo(String packageId, ItemScanData itemScan);
    
    /**
     * Get all scanned items for a package
     */
    List<ItemScanData> getScannedItemsForPackage(String packageId);
    
    /**
     * Data transfer object for video information
     */
    class VideoData {
        private String id;
        private String platformOrderId;
        private String status;
        private Instant createdAt;
        private Instant updatedAt;
        
        // Constructors, getters, setters
        public VideoData() {}
        
        public VideoData(String id, String platformOrderId, String status, Instant createdAt, Instant updatedAt) {
            this.id = id;
            this.platformOrderId = platformOrderId;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getPlatformOrderId() { return platformOrderId; }
        public void setPlatformOrderId(String platformOrderId) { this.platformOrderId = platformOrderId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    }
    
    /**
     * Data transfer object for item scan information
     */
    class ItemScanData {
        private int timestampOffsetSeconds;
        private String sku;
        private int quantity;
        private String status;
        private String barcodeEntityId;
        
        // Constructors, getters, setters
        public ItemScanData() {}
        
        public ItemScanData(int timestampOffsetSeconds, String sku, int quantity, String status, String barcodeEntityId) {
            this.timestampOffsetSeconds = timestampOffsetSeconds;
            this.sku = sku;
            this.quantity = quantity;
            this.status = status;
            this.barcodeEntityId = barcodeEntityId;
        }
        
        public int getTimestampOffsetSeconds() { return timestampOffsetSeconds; }
        public void setTimestampOffsetSeconds(int timestampOffsetSeconds) { this.timestampOffsetSeconds = timestampOffsetSeconds; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getBarcodeEntityId() { return barcodeEntityId; }
        public void setBarcodeEntityId(String barcodeEntityId) { this.barcodeEntityId = barcodeEntityId; }
    }
}