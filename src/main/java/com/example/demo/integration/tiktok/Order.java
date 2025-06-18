package com.example.demo.integration.tiktok;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public record Order(
    int code,
    String message,
    @JsonAlias("request_id")
    String requestId,
    OrderData data
) {
}

record OrderData(
    @JsonAlias("package_id")
    int packageId,
    @JsonAlias("order_info_list")
    List<OrderInfo> orderInfoList,
    @JsonAlias("package_status")
    int packageStatus,
    @JsonAlias("package_freeze_status")
    int packageFreezeStatus,
    @JsonAlias("sc_tag")
    int scTag,
    @JsonAlias("print_tag")
    int printTag,
    @JsonAlias("sku_tag")
    int skuTag,
    @JsonAlias("note_tag")
    int noteTag,
    @JsonAlias("delivery_option")
    int deliveryOption,
    @JsonAlias("shipping_provider")
    String shippingProvider,
    @JsonAlias("shipping_provider_id")
    String shippingProviderId,
    @JsonAlias("tracking_number")
    String trackingNumber,
    @JsonAlias("pick_up_type")
    int pickUpType,
    @JsonAlias("pick_up_start_time")
    long pickUpStartTime,
    @JsonAlias("pick_up_end_time")
    long pickUpEndTime,
    @JsonAlias("create_time")
    long createTime,
    @JsonAlias("update_time")
    long updateTime,
    @JsonAlias("order_line_id_list")
    List<String> orderLineIdList,
    @JsonAlias("cancel_because_logistic_issue")
    int cancelBecauseLogisticIssue
) {
}

record OrderInfo(
    @JsonAlias("order_id")
    String orderId,
    @JsonAlias("sku_list")
    List<SkuInfo> skuList
) {
}

record SkuInfo(
    @JsonAlias("sku_id")
    String skuId,
    @JsonAlias("sku_name")
    String skuName,
    @JsonAlias("sku_image")
    String skuImage,
    String quantity
) {
}