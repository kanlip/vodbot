package com.example.demo.product.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TikTokProductResponse {
    private Integer code;
    private String message;
    private String request_id;
    private TikTokProductData data;

    @Data
    public static class TikTokProductData {
        @JsonProperty("products")
        private List<TikTokProduct> products;
        
        @JsonProperty("next_page_token")
        private String nextPageToken;
        
        @JsonProperty("total_count")
        private Integer totalCount;
    }

    @Data
    public static class TikTokProduct {
        @JsonProperty("product_id")
        private String productId;
        
        @JsonProperty("product_name")
        private String productName;
        
        @JsonProperty("product_status")
        private String productStatus;
        
        @JsonProperty("create_time")
        private Long createTime;
        
        @JsonProperty("update_time")
        private Long updateTime;
        
        @JsonProperty("skus")
        private List<TikTokSku> skus;
        
        @JsonProperty("images")
        private List<TikTokImage> images;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("category_id")
        private String categoryId;
        
        @JsonProperty("brand_id")
        private String brandId;
    }

    @Data
    public static class TikTokSku {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("seller_sku")
        private String sellerSku;
        
        @JsonProperty("price")
        private TikTokPrice price;
        
        @JsonProperty("stock_info")
        private List<TikTokStockInfo> stockInfo;
        
        @JsonProperty("sales_attributes")
        private List<TikTokSalesAttribute> salesAttributes;
    }

    @Data
    public static class TikTokPrice {
        @JsonProperty("amount")
        private String amount;
        
        @JsonProperty("currency")
        private String currency;
    }

    @Data
    public static class TikTokStockInfo {
        @JsonProperty("available_stock")
        private Integer availableStock;
        
        @JsonProperty("warehouse_id")
        private String warehouseId;
    }

    @Data
    public static class TikTokSalesAttribute {
        @JsonProperty("attribute_id")
        private String attributeId;
        
        @JsonProperty("attribute_name")
        private String attributeName;
        
        @JsonProperty("value_id")
        private String valueId;
        
        @JsonProperty("value_name")
        private String valueName;
    }

    @Data
    public static class TikTokImage {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("thumb_urls")
        private List<String> thumbUrls;
        
        @JsonProperty("uri")
        private String uri;
        
        @JsonProperty("width")
        private Integer width;
        
        @JsonProperty("height")
        private Integer height;
    }
}