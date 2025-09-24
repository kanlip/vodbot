package com.example.demo.recording.adapter.in.web;

import com.example.demo.recording.domain.VideoShare;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class CreateShareRequest {
    private UUID sessionId;
    private VideoShare.ShareType shareType;
    private String recipient;
    private Instant expiresAt;
}