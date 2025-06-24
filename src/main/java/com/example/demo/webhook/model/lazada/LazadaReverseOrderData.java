package com.example.demo.webhook.model.lazada;

import com.fasterxml.jackson.annotation.JsonAlias;

public record LazadaReverseOrderData(
        @JsonAlias("order_status")
        String orderStatus,
        @JsonAlias("reverse_order_id")
        String reverseOrderId,
        @JsonAlias("reverse_order_line_id")
        String reverseOrderLineId,
        @JsonAlias("status_update_time")
        Long statusUpdateTime,
        @JsonAlias("trade_order_id")
        String tradeOrderId,
        @JsonAlias("trade_order_line_id")
        String tradeOrderLineId
) {}
