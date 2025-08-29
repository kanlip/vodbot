package com.example.demo.users.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class Organization {
    // getters and setters
    private String id;
    private String name;
    private String status;
    private Plan plan;
    private Billing billing;
    private Date createdAt;
    private Date updatedAt;
    @Setter
    @Getter
    public static class Plan {
        // getters and setters
        private String code;
        private Date startedAt;
        private Date expiresAt;

    }
    @Setter
    @Getter
    public static class Billing {
        // getters and setters
        private String contactEmail;
        private String billingCycle;
        private String currency;
    }

}
