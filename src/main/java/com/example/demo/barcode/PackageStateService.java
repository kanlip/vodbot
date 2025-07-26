package com.example.demo.barcode;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
@Slf4j
public class PackageStateService {
    
    private final Map<String, PackageState> activePackages = new ConcurrentHashMap<>();
    
    @Data
    public static class PackageState {
        private String packageId;
        private String presignedUrl;
        private LocalDateTime startTime;
        private boolean recordingStarted;
        
        public PackageState(String packageId, String presignedUrl) {
            this.packageId = packageId;
            this.presignedUrl = presignedUrl;
            this.startTime = LocalDateTime.now();
            this.recordingStarted = false;
        }
    }
    
    /**
     * Start a new package recording session
     */
    public PackageState startPackage(String packageId, String presignedUrl) {
        log.info("Starting new package session for packageId: {}", packageId);
        PackageState packageState = new PackageState(packageId, presignedUrl);
        activePackages.put(packageId, packageState);
        return packageState;
    }
    
    /**
     * Get active package state
     */
    public PackageState getActivePackage(String packageId) {
        return activePackages.get(packageId);
    }
    
    /**
     * Check if there's any active package (for determining if next scan is item verification)
     */
    public PackageState getAnyActivePackage() {
        return activePackages.values().stream()
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Mark recording as started for a package
     */
    public void markRecordingStarted(String packageId) {
        PackageState packageState = activePackages.get(packageId);
        if (packageState != null) {
            packageState.setRecordingStarted(true);
            log.info("Recording marked as started for packageId: {}", packageId);
        }
    }
    
    /**
     * End package session
     */
    public void endPackage(String packageId) {
        log.info("Ending package session for packageId: {}", packageId);
        activePackages.remove(packageId);
    }
    
    /**
     * Get all active packages (for debugging/monitoring)
     */
    public Map<String, PackageState> getAllActivePackages() {
        return new ConcurrentHashMap<>(activePackages);
    }
    
    /**
     * Clear all active packages (for testing/reset purposes)
     */
    public void clearAllPackages() {
        log.info("Clearing all active packages");
        activePackages.clear();
    }
}