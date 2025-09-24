package com.example.demo.platform.adapter.in.web;

import com.example.demo.shared.domain.Platform;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthUrlResponse {
    private final String authUrl;
    private final Platform platform;
}