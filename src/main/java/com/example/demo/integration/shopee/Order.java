package com.example.demo.integration.shopee;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public record Order(
        @JsonAlias("request_id")
        String requestId,
        String error,
        String message,
        Response response
) {
}

record Response(
        @JsonAlias("package_list")
        List<Package> packageList
) {
}

record Package(
        @JsonAlias("order_sn")
        String orderSn,
        @JsonAlias("package_number")
        String packageNumber,
        @JsonAlias("fulfillment_status")
        String fulfillmentStatus,
        @JsonAlias("update_time")
        Long updateTime,
        @JsonAlias("logistics_channel_id")
        Long logisticsChannelId,
        @JsonAlias("shipping_carrier")
        String shippingCarrier,
        @JsonAlias("allow_self_design_awb")
        Boolean allowSelfDesignAwb,
        @JsonAlias("days_to_ship")
        Long daysToShip,
        @JsonAlias("ship_by_date")
        Long shipByDate,
        @JsonAlias("pending_terms")
        List<String> pendingTerms,
        @JsonAlias("tracking_number")
        String trackingNumber,
        @JsonAlias("tracking_number_expiration_date")
        Long trackingNumberExpirationDate,
        @JsonAlias("pickup_done_time")
        Long pickupDoneTime,
        @JsonAlias("is_split_up")
        Boolean isSplitUp,
        @JsonAlias("item_list")
        List<Item> itemList,
        @JsonAlias("parcel_chargeable_weight_gram")
        Long parcelChargeableWeightGram,
        @JsonAlias("group_shipment_id")
        Long groupShipmentId,
        @JsonAlias("virtual_contact_number")
        String virtualContactNumber,
        @JsonAlias("package_query_number")
        String packageQueryNumber,
        String warning
) {
}

record Item(
        @JsonAlias("item_id")
        Long itemId,
        @JsonAlias("model_id")
        Long modelId,
        @JsonAlias("item_sku")
        String itemSku,
        @JsonAlias("model_sku")
        String modelSku,
        @JsonAlias("model_quantity")
        Long modelQuantity,
        @JsonAlias("order_item_id")
        Long orderItemId,
        @JsonAlias("promotion_group_id")
        Long promotionGroupId,
        @JsonAlias("product_location_id")
        String productLocationId
) {
}

record RecipientAddress(
        String name,
        String phone,
        String town,
        String district,
        String city,
        String state,
        String region,
        String zipcode,
        @JsonAlias("full_address")
        String fullAddress
) {
}