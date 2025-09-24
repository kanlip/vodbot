package com.example.demo.platform.port.out;

import com.example.demo.platform.domain.PlatformIntegration;
import com.example.demo.shared.domain.Platform;

public interface PlatformApiClientFactory {
    PlatformApiClient createClient(Platform platform, PlatformIntegration integration);
}