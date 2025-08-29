package com.example.demo.order.domain;

import com.example.demo.shared.domain.Platform;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
public class Order {
    private String id;
    private String sellerId;
    private String platformOrderId;
    private Instant orderDate;
    private Platform platform;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> packageIds;

}

