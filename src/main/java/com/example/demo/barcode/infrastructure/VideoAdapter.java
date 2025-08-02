package com.example.demo.barcode.infrastructure;

import com.example.demo.barcode.domain.VideoPort;
import com.example.demo.video.entity.VideoEntity;
import com.example.demo.video.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Infrastructure adapter implementing VideoPort using the video repository
 */
@Component
@Slf4j
public class VideoAdapter implements VideoPort {

    private final VideoRepository videoRepository;

    public VideoAdapter(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Override
    public VideoData findOrCreateVideoForPackage(String packageId) {
        // Try to find existing video entity by platform order ID
        Optional<VideoEntity> existingVideo = videoRepository.findAll().stream()
                .filter(video -> packageId.equals(video.getPlatformOrderId()))
                .findFirst();
        
        if (existingVideo.isPresent()) {
            log.debug("Found existing video entity for package: {}", packageId);
            return mapToVideoData(existingVideo.get());
        }
        
        // Create new video entity
        log.info("Creating new video entity for package: {}", packageId);
        VideoEntity newVideo = new VideoEntity();
        newVideo.setPlatformOrderId(packageId);
        newVideo.setStatus("recording");
        newVideo.setCreatedAt(Instant.now());
        newVideo.setUpdatedAt(Instant.now());
        newVideo.setItemScans(new ArrayList<>());
        
        VideoEntity savedVideo = videoRepository.save(newVideo);
        return mapToVideoData(savedVideo);
    }

    @Override
    public void saveItemScanToVideo(String packageId, ItemScanData itemScan) {
        log.info("Saving item scan to video for package: {}, sku: {}", packageId, itemScan.getSku());
        
        // Find or create video entity for this package
        VideoEntity videoEntity = findOrCreateVideoEntity(packageId);
        
        // Create item scan record
        VideoEntity.ItemScan videoItemScan = new VideoEntity.ItemScan();
        videoItemScan.setTimestampOffsetSeconds(itemScan.getTimestampOffsetSeconds());
        videoItemScan.setSku(itemScan.getSku());
        videoItemScan.setQuantity(itemScan.getQuantity());
        videoItemScan.setStatus(itemScan.getStatus());
        if (itemScan.getBarcodeEntityId() != null) {
            videoItemScan.setBarcodeEntityId(new org.bson.types.ObjectId(itemScan.getBarcodeEntityId()));
        }
        
        // Add to video entity
        if (videoEntity.getItemScans() == null) {
            videoEntity.setItemScans(new ArrayList<>());
        }
        videoEntity.getItemScans().add(videoItemScan);
        videoEntity.setUpdatedAt(Instant.now());
        
        // Save video entity
        videoRepository.save(videoEntity);
        
        log.info("Item scan saved successfully: {}", videoItemScan);
    }

    @Override
    public List<ItemScanData> getScannedItemsForPackage(String packageId) {
        Optional<VideoEntity> video = videoRepository.findAll().stream()
                .filter(v -> packageId.equals(v.getPlatformOrderId()))
                .findFirst();
        
        if (video.isPresent() && video.get().getItemScans() != null) {
            return video.get().getItemScans().stream()
                    .map(this::mapToItemScanData)
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }

    private VideoEntity findOrCreateVideoEntity(String packageId) {
        Optional<VideoEntity> existingVideo = videoRepository.findAll().stream()
                .filter(video -> packageId.equals(video.getPlatformOrderId()))
                .findFirst();
        
        if (existingVideo.isPresent()) {
            return existingVideo.get();
        }
        
        // Create new video entity
        VideoEntity newVideo = new VideoEntity();
        newVideo.setPlatformOrderId(packageId);
        newVideo.setStatus("recording");
        newVideo.setCreatedAt(Instant.now());
        newVideo.setUpdatedAt(Instant.now());
        newVideo.setItemScans(new ArrayList<>());
        
        return videoRepository.save(newVideo);
    }

    private VideoData mapToVideoData(VideoEntity entity) {
        return new VideoData(
            entity.getId(),
            entity.getPlatformOrderId(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private ItemScanData mapToItemScanData(VideoEntity.ItemScan itemScan) {
        return new ItemScanData(
            itemScan.getTimestampOffsetSeconds(),
            itemScan.getSku(),
            itemScan.getQuantity(),
            itemScan.getStatus(),
            itemScan.getBarcodeEntityId() != null ? itemScan.getBarcodeEntityId().toString() : null
        );
    }
}