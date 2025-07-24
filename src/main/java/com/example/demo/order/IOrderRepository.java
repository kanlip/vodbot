package com.example.demo.order;

import com.example.demo.common.Platform;
import com.example.demo.order.entity.OrderEntity;

import java.util.Optional;

public interface IOrderRepository {

    Optional<OrderEntity> findByPlatformOrderIdAndPlatform(String id, Platform platform);
    OrderEntity save(OrderEntity orderEntity);
}
