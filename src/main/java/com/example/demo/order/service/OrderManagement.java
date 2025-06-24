package com.example.demo.order.service;

import com.example.demo.order.entity.Order;
import com.example.demo.order.repository.OrderRepository;
import com.example.demo.webhook.model.lazada.LazadaTradeOrderData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderManagement {

    private final OrderRepository orderRepository; // Assuming you have an OrderRepository to handle database operations
    @ApplicationModuleListener
    void on(LazadaTradeOrderData data) {
        // Process the LazadaTradeOrderData
        log.debug("Received Lazada Trade Order Data: {}", data);
        Optional<Order> order = orderRepository.findByPlatformOrderId(data.tradeOrderId());
        if(order.isEmpty()) {
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



        // Here you can add logic to handle the order data, such as saving it to a database or processing it further.
    }
}
