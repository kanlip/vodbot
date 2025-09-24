package com.example.demo.recording.adapter.in.web;

import com.example.demo.recording.domain.RecordingSession;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StartSessionRequest {
    private UUID orderId;
    private UUID packerId;
    private RecordingSession.SessionType sessionType = RecordingSession.SessionType.PACKING;
}