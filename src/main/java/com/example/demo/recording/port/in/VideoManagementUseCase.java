package com.example.demo.recording.port.in;

import com.example.demo.recording.domain.VideoFile;
import com.example.demo.recording.domain.VideoShare;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface VideoManagementUseCase {
    VideoFile uploadVideo(UploadVideoCommand command);
    VideoFile processVideo(UUID videoFileId);
    List<VideoFile> getSessionVideos(UUID sessionId);
    VideoFile getVideoFile(UUID videoFileId);
    void deleteVideo(UUID videoFileId);

    VideoShare createShare(CreateShareCommand command);
    VideoShare getShare(String shareToken);
    void recordShareAccess(String shareToken);
    void deactivateShare(UUID shareId);
    List<VideoShare> getSessionShares(UUID sessionId);
}