package com.example.demo.recording.port.out;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

public interface VideoStorageService {
    String uploadVideo(String bucket, String key, InputStream inputStream, String contentType, long contentLength);
    InputStream downloadVideo(String bucket, String key);
    URL generatePresignedUrl(String bucket, String key, Duration expiration);
    void deleteVideo(String bucket, String key);
    boolean videoExists(String bucket, String key);
    long getVideoSize(String bucket, String key);
}