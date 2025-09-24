package com.example.demo.users.domain;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private UUID id;
    private UUID orgId;
    private String email;
    private String passwordHash;
    private List<String> roles;
    private String displayName;
    private boolean isSupervisor;
    private String supervisorPinHash;
    private Instant lastLoginAt;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}

