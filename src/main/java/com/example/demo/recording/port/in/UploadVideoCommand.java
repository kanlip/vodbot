package com.example.demo.recording.port.in;

import com.example.demo.recording.domain.VideoFile;
import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;
import java.util.UUID;

@Getter
@Builder
public class UploadVideoCommand {
    private final UUID sessionId;
    private final String fileName;
    private final InputStream fileStream;
    private final Long fileSizeBytes;
    private final String contentType;
    private final Integer durationSeconds;
    private final String resolution;
}