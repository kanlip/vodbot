package com.example.demo.barcode.application;

import com.example.demo.barcode.domain.VideoPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service for retrieving scanned items
 */
@Service
@Slf4j
public class ScannedItemsQueryService {

    private final VideoPort videoPort;

    public ScannedItemsQueryService(VideoPort videoPort) {
        this.videoPort = videoPort;
    }

    /**
     * Get all scanned items for a package
     */
    public List<VideoPort.ItemScanData> getScannedItems(String packageId) {
        log.info("Getting scanned items for package: {}", packageId);
        
        try {
            return videoPort.getScannedItemsForPackage(packageId);
        } catch (Exception e) {
            log.error("Error retrieving scanned items for package {}: {}", packageId, e.getMessage(), e);
            throw new ScannedItemsQueryException(
                "Error retrieving scanned items: " + e.getMessage(), e
            );
        }
    }

    /**
     * Exception for scanned items query errors
     */
    public static class ScannedItemsQueryException extends RuntimeException {
        public ScannedItemsQueryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}