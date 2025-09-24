package com.example.demo.platform.port.in;

import com.example.demo.platform.domain.PlatformIntegration;

import java.util.List;
import java.util.UUID;

public interface PlatformAuthorizationUseCase {
    String initiateOAuthFlow(InitiateOAuthCommand command);
    PlatformIntegration completeOAuthFlow(CompleteOAuthCommand command);
    PlatformIntegration refreshAccessToken(UUID integrationId);
    void revokeIntegration(UUID integrationId);
    PlatformIntegration getIntegration(UUID integrationId);
    List<PlatformIntegration> getOrganizationIntegrations(UUID orgId);
    List<PlatformIntegration> getAuthorizedIntegrations(UUID orgId);
}