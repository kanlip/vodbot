package com.example.demo.integration;

import java.util.List;

public interface OrderRepository<T, ID> {

    List<T> getOrdersByTrackingId(ID trackingId);
}
