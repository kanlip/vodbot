package com.example.demo.webhook.model.lazada;

import com.fasterxml.jackson.annotation.JsonAlias;

public record LazadaTradeOrderData(
        @JsonAlias("order_status")
        String orderStatus,
        @JsonAlias("status_update_time")
        Long statusUpdateTime,
        @JsonAlias("trade_order_id")
        String tradeOrderId,
        @JsonAlias("trade_order_line_id")
        String tradeOrderLineId
) {}
