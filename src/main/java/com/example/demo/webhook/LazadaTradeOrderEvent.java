package com.example.demo.webhook;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.jmolecules.event.types.DomainEvent;

public record LazadaTradeOrderEvent(
        @JsonAlias("order_status")
        String orderStatus,
        @JsonAlias("status_update_time")
        Long statusUpdateTime,
        @JsonAlias("trade_order_id")
        String tradeOrderId,
        @JsonAlias("trade_order_line_id")
        String tradeOrderLineId
) implements DomainEvent {

}
