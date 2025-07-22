package com.example.demo.product.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class TikTokSyncProductRequest extends TikTokCommonParameter {

    @JsonProperty("page_size")
    private Integer pageSize;

    @JsonProperty("page_token")
    private String pageToken;

    // Request body for the API call
    private TikTokSearchRequestBody requestBody;
}
