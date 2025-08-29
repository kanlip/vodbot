package com.example.demo.order.application;

import com.example.demo.shared.domain.Platform;
import com.example.demo.order.adapter.out.persistence.OrderEntity;
import com.example.demo.order.port.out.OrderRepository;
import com.example.demo.webhook.events.LazadaTradeOrderEvent;
import com.example.demo.webhook.events.ShopeeOrderEvent;
import com.example.demo.order.domain.Order;
import com.example.demo.order.adapter.out.persistence.OrderMapper;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderManagement {

    private final OrderRepository orderRepository; // Assuming you have an OrderRepository to handle database operations

    @Async
    @EventListener
    void onLazadaOrderEvent(LazadaTradeOrderEvent data) {
        log.info("Received Lazada Trade Order Data: {}", data);
        OrderEntity orderEntityOpt = orderRepository.findByPlatform(
                Platform.LAZADA,
                data.tradeOrderId(),
                data.sellerId()
        );
        Order order;
        if (Objects.nonNull(orderEntityOpt)) {

            Order ord = new Order();
            ord.setPlatform(Platform.LAZADA);
            ord.setUpdatedAt(Instant.ofEpochMilli(data.statusUpdateTime()));
            ord.setPlatformOrderId(data.tradeOrderId());
            ord.setSellerId(data.sellerId());
            ord.setStatus(data.orderStatus());
            order = orderRepository.update(ord);
        } else {
            Order ord = new Order();
            ord.setUpdatedAt(Instant.ofEpochMilli(data.statusUpdateTime()));
            ord.setPlatform(Platform.LAZADA);
            ord.setPlatformOrderId(data.tradeOrderId());
            ord.setSellerId(data.sellerId());
            ord.setStatus(data.orderStatus());
            order = orderRepository.save(ord);
        }
    }

    @Async
    @EventListener
    void onShopeeOrderEvent(ShopeeOrderEvent data) {
        log.info("Received Shopee Order Data: {}", data);
        OrderEntity orderEntityOpt =
                orderRepository.findByPlatform(
                        Platform.SHOPEE,
                        data.orderId(),
                        Strings.EMPTY
                );
        Order order;
        if (Objects.nonNull(orderEntityOpt)) {
            Order ord = new Order();
            ord.setPlatform(Platform.SHOPEE);
            ord.setUpdatedAt(Instant.ofEpochMilli(data.updateTime()));
            ord.setPlatformOrderId(data.orderId());
            ord.setStatus(data.orderStatus());
            order = orderRepository.update(ord);
        } else {
            Order ord = new Order();
            ord.setPlatform(Platform.SHOPEE);
            ord.setUpdatedAt(Instant.ofEpochMilli(data.updateTime()));
            ord.setPlatformOrderId(data.orderId());
            ord.setStatus(data.orderStatus());
            order = orderRepository.save(ord);
        }
    }
}
