package com.example.demo.product.internal;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class LazadaSyncProductRequest extends LazadaCommonParameter {
    @JsonProperty("filter")
    private String filter;

    @JsonProperty("update_before")
    private String updateBefore;

    @JsonProperty("create_before")
    private String createBefore;

    @JsonProperty("offset")
    private String offset;

    @JsonProperty("create_after")
    private String createAfter;

    @JsonProperty("update_after")
    private String updateAfter;

    @JsonProperty("limit")
    private String limit;

    @JsonProperty("options")
    private String options;

    @JsonProperty("sku_seller_list")
    private String skuSellerList;
}
