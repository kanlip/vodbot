package com.example.demo.platform.port.in;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompleteOAuthCommand {
    private final String state;
    private final String authorizationCode;
    private final String shopId;
}