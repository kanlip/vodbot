package com.example.demo.recording.adapter.in;

import com.example.demo.recording.port.in.VideoUploadUseCase;
import com.example.demo.recording.port.in.VideoUploadUseCase.VideoUploadCompletionEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/s3-events")
@RequiredArgsConstructor
@Slf4j
public class S3EventController {

    private final VideoUploadUseCase videoUploadUseCase;
    private final ObjectMapper objectMapper;

    @PostMapping("/object-created")
    public ResponseEntity<Void> handleS3ObjectCreated(@RequestBody String eventPayload) {
        log.info("Received S3 object created event");

        try {
            JsonNode eventNode = objectMapper.readTree(eventPayload);

            // Handle SNS-wrapped events
            if (eventNode.has("Records")) {
                processS3Records(eventNode.get("Records"));
            } else if (eventNode.has("Message")) {
                // SNS wrapped S3 event
                String message = eventNode.get("Message").asText();
                JsonNode messageNode = objectMapper.readTree(message);
                if (messageNode.has("Records")) {
                    processS3Records(messageNode.get("Records"));
                }
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Failed to process S3 event: {}", eventPayload, e);
            // Return 200 to prevent retry loops for malformed events
            return ResponseEntity.ok().build();
        }
    }

    private void processS3Records(JsonNode records) {
        for (JsonNode record : records) {
            try {
                JsonNode s3Node = record.get("s3");
                if (s3Node == null) {
                    continue;
                }

                String eventName = record.get("eventName").asText();

                // Only process object created events
                if (!eventName.startsWith("s3:ObjectCreated:")) {
                    continue;
                }

                JsonNode bucketNode = s3Node.get("bucket");
                JsonNode objectNode = s3Node.get("object");

                if (bucketNode == null || objectNode == null) {
                    continue;
                }

                String bucketName = bucketNode.get("name").asText();
                String objectKey = objectNode.get("key").asText();
                long objectSize = objectNode.get("size").asLong();

                // Only process video files in our video prefix
                if (!objectKey.startsWith("videos/")) {
                    log.debug("Ignoring non-video S3 object: {}", objectKey);
                    continue;
                }

                log.info("Processing S3 object created event for bucket: {} key: {}", bucketName, objectKey);

                // Create upload completion event
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("eventName", eventName);
                metadata.put("eventTime", record.get("eventTime").asText());

                VideoUploadCompletionEvent event = new VideoUploadCompletionEvent(
                        objectKey,
                        bucketName,
                        objectSize,
                        detectContentType(objectKey),
                        metadata
                );

                videoUploadUseCase.handleUploadCompletion(event);

            } catch (Exception e) {
                log.error("Failed to process S3 record: {}", record, e);
            }
        }
    }

    private String detectContentType(String objectKey) {
        String lowerKey = objectKey.toLowerCase();
        if (lowerKey.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerKey.endsWith(".webm")) {
            return "video/webm";
        } else if (lowerKey.endsWith(".mov")) {
            return "video/quicktime";
        } else if (lowerKey.endsWith(".avi")) {
            return "video/x-msvideo";
        }
        return "application/octet-stream";
    }
}