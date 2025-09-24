package com.example.demo.recording.adapter.in.web;

import com.example.demo.recording.domain.RecordingSession;
import com.example.demo.recording.port.in.RecordingSessionUseCase;
import com.example.demo.recording.port.in.StartSessionCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recording/sessions")
@RequiredArgsConstructor
@Tag(name = "Recording Sessions", description = "Video recording session management")
public class RecordingSessionController {

    private final RecordingSessionUseCase recordingSessionUseCase;

    @PostMapping
    @Operation(summary = "Start a new recording session")
    public ResponseEntity<RecordingSession> startSession(@RequestBody StartSessionRequest request) {
        StartSessionCommand command = StartSessionCommand.builder()
                .orderId(request.getOrderId())
                .packerId(request.getPackerId())
                .sessionType(request.getSessionType())
                .build();

        RecordingSession session = recordingSessionUseCase.startSession(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @PutMapping("/{sessionId}/end")
    @Operation(summary = "End a recording session")
    public ResponseEntity<RecordingSession> endSession(@PathVariable UUID sessionId) {
        RecordingSession session = recordingSessionUseCase.endSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get recording session details")
    public ResponseEntity<RecordingSession> getSession(@PathVariable UUID sessionId) {
        RecordingSession session = recordingSessionUseCase.getSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping
    @Operation(summary = "Get recording sessions by order or packer")
    public ResponseEntity<List<RecordingSession>> getSessions(
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) UUID packerId) {

        List<RecordingSession> sessions;

        if (orderId != null) {
            sessions = recordingSessionUseCase.getSessionsByOrder(orderId);
        } else if (packerId != null) {
            sessions = recordingSessionUseCase.getSessionsByPacker(packerId);
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(sessions);
    }

    @PutMapping("/{sessionId}/complete")
    @Operation(summary = "Mark session as completed")
    public ResponseEntity<Void> markCompleted(@PathVariable UUID sessionId) {
        recordingSessionUseCase.markSessionCompleted(sessionId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{sessionId}/fail")
    @Operation(summary = "Mark session as failed")
    public ResponseEntity<Void> markFailed(
            @PathVariable UUID sessionId,
            @RequestBody FailSessionRequest request) {
        recordingSessionUseCase.markSessionFailed(sessionId, request.getReason());
        return ResponseEntity.ok().build();
    }
}