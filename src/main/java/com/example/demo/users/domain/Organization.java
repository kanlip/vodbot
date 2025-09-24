package com.example.demo.users.domain;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {
    private UUID id;
    private String name;
    private String status;
    private Plan plan;
    private Billing billing;
    private Instant createdAt;
    private Instant updatedAt;

    public enum Plan {
        FREE, BASIC, PREMIUM, ENTERPRISE
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Billing {
        private String contactEmail;
        private String billingCycle;
        private String currency;
    }
}
