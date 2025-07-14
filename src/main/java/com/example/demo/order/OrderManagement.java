package com.example.demo.order;

import com.example.demo.order.entity.OrderEntity;
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

    private final IOrderRepository orderRepository; // Assuming you have an OrderRepository to handle database operations
    @Async
    @EventListener
    void onLazadaOrderEvent(LazadaTradeOrderEvent data) {
        // Process the LazadaTradeOrderData
        log.info("Received Lazada Trade Order Data: {}", data);
        Optional<OrderEntity> order = orderRepository
                .findByPlatformOrderIdAndPlatform(data.tradeOrderId(), Platform.LAZADA);
        if(order.isPresent()) {
            OrderEntity existingOrderEntity = order.get();
            existingOrderEntity.setStatus(data.orderStatus());
            existingOrderEntity.setUpdatedAt(Instant.ofEpochMilli(data.statusUpdateTime()));
            existingOrderEntity.setPlatformOrderId(data.tradeOrderId());
            existingOrderEntity.setPlatform(Platform.LAZADA);
            orderRepository.save(existingOrderEntity);
        } else{
            OrderEntity newOrderEntity = OrderEntity.builder()
                    .status(data.orderStatus())
                    .updatedAt(Instant.ofEpochMilli(data.statusUpdateTime()))
                    .platformOrderId(data.tradeOrderId())
                    .platform(Platform.LAZADA)
                    .build();
            orderRepository.save(newOrderEntity);
        }

    }

    @Async
    @EventListener
    void onShopeeOrderEvent(ShopeeOrderEvent data) {
        // Process the ShopeeOrderData
        log.info("Received Shopee Order Data: {}", data);
        Optional<OrderEntity> order = orderRepository
                .findByPlatformOrderIdAndPlatform(data.orderId(), Platform.SHOPEE);
        if(order.isPresent()) {
            OrderEntity existingOrderEntity = order.get();
            existingOrderEntity.setStatus(data.orderStatus());
            existingOrderEntity.setUpdatedAt(Instant.ofEpochMilli(data.updateTime()));
            existingOrderEntity.setPlatformOrderId(data.orderId());
            existingOrderEntity.setPlatform(Platform.SHOPEE);
            orderRepository.save(existingOrderEntity);
        } else {
            OrderEntity newOrderEntity = OrderEntity.builder()
                    .status(data.orderStatus())
                    .updatedAt(Instant.ofEpochMilli(data.updateTime()))
                    .platformOrderId(data.orderId())
                    .platform(Platform.SHOPEE)
                    .build();
            orderRepository.save(newOrderEntity);
        }
    }
}
