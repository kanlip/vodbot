package com.example.demo.integration;

import java.util.List;

public interface OrderIntegrationRepository<T, ID> {
    List<T> getOrdersByTrackingId(ID packageId);
}
