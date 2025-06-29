package com.example.demo.order;

import com.example.demo.order.internal.Order;
import com.example.demo.order.internal.Platform;
import com.example.demo.webhook.LazadaTradeOrderEvent;
import com.example.demo.webhook.ShopeeOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderManagement {

    private final OrderRepository orderRepository; // Assuming you have an OrderRepository to handle database operations
    @Async
    @EventListener
    void onLazadaOrderEvent(LazadaTradeOrderEvent data) {
        // Process the LazadaTradeOrderData
        log.info("Received Lazada Trade Order Data: {}", data);
        Optional<Order> order = orderRepository
                .findByPlatformOrderIdAndPlatform(data.tradeOrderId(), Platform.LAZADA);
        if(order.isPresent()) {
            Order existingOrder = order.get();
            existingOrder.setStatus(data.orderStatus());
            existingOrder.setUpdatedAt(Instant.ofEpochMilli(data.statusUpdateTime()));
            existingOrder.setPlatformOrderId(data.tradeOrderId());
            existingOrder.setPlatform(Platform.LAZADA);
            orderRepository.save(existingOrder);
        } else{
            Order newOrder = Order.builder()
                    .status(data.orderStatus())
                    .updatedAt(Instant.ofEpochMilli(data.statusUpdateTime()))
                    .platformOrderId(data.tradeOrderId())
                    .platform(Platform.LAZADA)
                    .build();
            orderRepository.save(newOrder);
        }

    }

    @Async
    @EventListener
    void onShopeeOrderEvent(ShopeeOrderEvent data) {
        // Process the ShopeeOrderData
        log.info("Received Shopee Order Data: {}", data);
        Optional<Order> order = orderRepository
                .findByPlatformOrderIdAndPlatform(data.orderId(), Platform.SHOPEE);
        if(order.isPresent()) {
            Order existingOrder = order.get();
            existingOrder.setStatus(data.orderStatus());
            existingOrder.setUpdatedAt(Instant.ofEpochMilli(data.updateTime()));
            existingOrder.setPlatformOrderId(data.orderId());
            existingOrder.setPlatform(Platform.SHOPEE);
            orderRepository.save(existingOrder);
        } else {
            Order newOrder = Order.builder()
                    .status(data.orderStatus())
                    .updatedAt(Instant.ofEpochMilli(data.updateTime()))
                    .platformOrderId(data.orderId())
                    .platform(Platform.SHOPEE)
                    .build();
            orderRepository.save(newOrder);
        }
    }
}
