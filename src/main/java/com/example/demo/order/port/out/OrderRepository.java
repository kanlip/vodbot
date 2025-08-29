package com.example.demo.order.port.out;

import com.example.demo.order.adapter.out.persistence.OrderEntity;
import com.example.demo.order.domain.Order;
import com.example.demo.shared.domain.Platform;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order findById(UUID id);
    List<Order> findAll();
    Order save(Order order);
    Order update(Order order);
    OrderEntity findByPlatform(Platform platform, String platformOrderId, String sellerId);
}
