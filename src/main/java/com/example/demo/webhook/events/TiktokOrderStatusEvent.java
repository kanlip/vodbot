package com.example.demo.webhook.events;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.jmolecules.event.types.DomainEvent;

public record TiktokOrderStatusEvent(
    @JsonAlias("order_id") String orderId,
    @JsonAlias("order_status") String orderStatus,
    @JsonAlias("is_on_hold_order") boolean isOnHoldOrder,
    @JsonAlias("update_time") long updateTime
) implements DomainEvent {}
