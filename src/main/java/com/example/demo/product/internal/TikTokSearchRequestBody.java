package com.example.demo.product.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class TikTokSearchRequestBody {
    
    private String status;
    
    @JsonProperty("seller_skus")
    private List<String> sellerSkus;
    
    @JsonProperty("create_time_ge")
    private Long createTimeGe;
    
    @JsonProperty("create_time_le")
    private Long createTimeLe;
    
    @JsonProperty("update_time_ge")
    private Long updateTimeGe;
    
    @JsonProperty("update_time_le")
    private Long updateTimeLe;
    
    @JsonProperty("category_version")
    private String categoryVersion;
    
    @JsonProperty("listing_quality_tiers")
    private List<String> listingQualityTiers;
    
    @JsonProperty("listing_platforms")
    private List<String> listingPlatforms;
    
    @JsonProperty("audit_status")
    private String auditStatus;
    
    @JsonProperty("sku_ids")
    private List<String> skuIds;
}