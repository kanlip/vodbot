package com.example.demo.webhook.events;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jmolecules.event.types.DomainEvent;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShopeeOrderEvent(
    @JsonAlias("ordersn") String orderId,
    @JsonAlias("status") String orderStatus,
    @JsonAlias("completed_scenario") String completedScenario,
    @JsonAlias("update_time") Long updateTime
) implements DomainEvent {}
