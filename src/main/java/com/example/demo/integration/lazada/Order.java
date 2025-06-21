package com.example.demo.integration.lazada;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public record Order(
        Result result,
        String code,
        @JsonAlias("request_id")
        String requestId
) {
}

record Result(
        @JsonAlias("error_msg")
        String errorMsg,
        Data data,
        String success,
        @JsonAlias("error_code")
        String errorCode
) {
}

record Data(
        @JsonAlias("pack_order_list")
        List<PackOrder> packOrderList
) {
}

record PackOrder(
        @JsonAlias("order_item_list")
        List<OrderItem> orderItemList,
        @JsonAlias("order_id")
        String orderId
) {
}

record OrderItem(
        @JsonAlias("order_item_id")
        String orderItemId,
        String msg,
        @JsonAlias("item_err_code")
        String itemErrCode,
        @JsonAlias("tracking_number")
        String trackingNumber,
        @JsonAlias("shipment_provider")
        String shipmentProvider,
        @JsonAlias("package_id")
        String packageId,
        String retry
) {
}