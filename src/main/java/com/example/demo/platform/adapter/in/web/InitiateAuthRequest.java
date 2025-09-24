package com.example.demo.platform.adapter.in.web;

import com.example.demo.shared.domain.Platform;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class InitiateAuthRequest {
    private Platform platform;
    private String sellerId; // Optional - for Lazada this will be null and populated from OAuth response
    private String redirectUrl;
    private String webhookSecret;
}