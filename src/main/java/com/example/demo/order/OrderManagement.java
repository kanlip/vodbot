package com.example.demo.order;

import com.example.demo.order.internal.Order;
import com.example.demo.webhook.LazadaTradeOrderData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
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
    void on(LazadaTradeOrderData data) {
        // Process the LazadaTradeOrderData
        log.info("Received Lazada Trade Order Data: {}", data);
        Optional<Order> order = orderRepository.findByPlatformOrderId(data.tradeOrderId());
        if(order.isPresent()) {
            Order existingOrder = order.get();
            existingOrder.setStatus(data.orderStatus());
            existingOrder.setUpdatedAt(Instant.ofEpochMilli(data.statusUpdateTime()));
            existingOrder.setPlatformOrderId(data.tradeOrderId());
            existingOrder.setPlatform("Lazada");
            orderRepository.save(existingOrder);
        } else{
            Order newOrder = Order.builder()
                    .status(data.orderStatus())
                    .updatedAt(Instant.ofEpochMilli(data.statusUpdateTime()))
                    .platformOrderId(data.tradeOrderId())
                    .platform("Lazada")
                    .build();
            orderRepository.save(newOrder);
        }

    }
}
