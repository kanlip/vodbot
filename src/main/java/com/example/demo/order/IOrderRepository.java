package com.example.demo.order;

import com.example.demo.order.internal.Order;
import com.example.demo.order.internal.Platform;

import java.util.Optional;

public interface IOrderRepository {

    Optional<Order> findByPlatformOrderIdAndPlatform(String id, Platform platform);
    Order save(Order order);
}
