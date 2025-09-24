package com.example.demo.recording.port.out;

import com.example.demo.recording.domain.VideoFile;
import com.example.demo.recording.domain.QualityMetric;

import java.util.List;
import java.util.UUID;

public interface VideoProcessingService {
    void processVideo(UUID videoFileId);
    VideoFile generateThumbnail(UUID videoFileId);
    VideoFile compressVideo(UUID videoFileId, String targetResolution);
    List<QualityMetric> analyzeVideoQuality(UUID videoFileId);
    boolean isProcessingSupported(String format);
}