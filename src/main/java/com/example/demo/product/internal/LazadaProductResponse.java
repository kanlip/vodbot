package com.example.demo.product.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class LazadaProductResponse {
    @JsonProperty("code")
    private String code;

    @JsonProperty("data")
    private LazadaProductData data;

    @JsonProperty("request_id")
    private String requestId;

    @Data
    public static class LazadaProductData {
        @JsonProperty("total_products")
        private String totalProducts;

        @JsonProperty("products")
        private List<LazadaProduct> products;
    }

    @Data
    public static class LazadaProduct {
        @JsonProperty("created_time")
        private String createdTime;
        @JsonProperty("updated_time")
        private String updatedTime;
        @JsonProperty("images")
        private String images;
        @JsonProperty("skus")
        private List<Sku> skus;
        @JsonProperty("item_id")
        private String itemId;
        @JsonProperty("hiddenStatus")
        private String hiddenStatus;
        @JsonProperty("suspendedSkus")
        private List<String> suspendedSkus;
        @JsonProperty("subStatus")
        private String subStatus;
        @JsonProperty("trialProduct")
        private String trialProduct;
        @JsonProperty("rejectReason")
        private List<RejectReason> rejectReason;
        @JsonProperty("primary_category")
        private String primaryCategory;
        @JsonProperty("marketImages")
        private String marketImages;
        @JsonProperty("attributes")
        private Attributes attributes;
        @JsonProperty("hiddenReason")
        private String hiddenReason;
        @JsonProperty("status")
        private String status;
    }

    @Data
    public static class Sku {
        @JsonProperty("Status")
        private String status;
        @JsonProperty("quantity")
        private Integer quantity;
        @JsonProperty("product_weight")
        private String productWeight;
        @JsonProperty("Images")
        private List<String> images;
        @JsonProperty("SellerSku")
        private String sellerSku;
        @JsonProperty("ShopSku")
        private String shopSku;
        @JsonProperty("Url")
        private String url;
        @JsonProperty("package_width")
        private String packageWidth;
        @JsonProperty("special_to_time")
        private String specialToTime;
        @JsonProperty("special_from_time")
        private String specialFromTime;
        @JsonProperty("package_height")
        private String packageHeight;
        @JsonProperty("special_price")
        private Integer specialPrice;
        @JsonProperty("price")
        private Integer price;
        @JsonProperty("package_length")
        private String packageLength;
        @JsonProperty("package_weight")
        private String packageWeight;
        @JsonProperty("Available")
        private Integer available;
        @JsonProperty("SkuId")
        private Integer skuId;
        @JsonProperty("special_to_date")
        private String specialToDate;
    }

    @Data
    public static class RejectReason {
        @JsonProperty("suggestion")
        private String suggestion;
        @JsonProperty("violationDetail")
        private String violationDetail;
    }

    @Data
    public static class Attributes {
        @JsonProperty("short_description")
        private String shortDescription;
        @JsonProperty("name")
        private String name;
        @JsonProperty("description")
        private String description;
        @JsonProperty("name_engravement")
        private String nameEngravement;
        @JsonProperty("warranty_type")
        private String warrantyType;
        @JsonProperty("gift_wrapping")
        private String giftWrapping;
        @JsonProperty("preorder_days")
        private Integer preorderDays;
        @JsonProperty("brand")
        private String brand;
        @JsonProperty("preorder")
        private String preorder;
    }
}
