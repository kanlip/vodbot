package com.example.demo.user.entity;


import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document(collection = "companies")
public class CompanyEntity {
    @Id
    private ObjectId id;

    private String name;
    private String mainContactEmail;
    private String phone;
    private Address address;
    private Subscription subscription;
    private Settings settings;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    public static class Address {
        private String street;
        private String city;
        private String zip;
        private String country;
    }

    @Data
    @Builder
    public static class Subscription {
        private String plan;
        private String status;
        private Instant startDate;
        private Instant endDate;
    }

    @Data
    @Builder
    public static class Settings {
        private Integer videoRetentionDays;
        private String defaultTimezone;
        private String cutOffTime;
    }
}
