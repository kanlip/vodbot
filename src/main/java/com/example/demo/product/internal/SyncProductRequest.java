package com.example.demo.product.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class SyncProductRequest extends CommonParameter{

    private int offset;
    @JsonProperty("page_size")
    private int pageSize;
    @JsonProperty("update_time_from")
    private String updateTimeFrom;
    @JsonProperty("update_time_to")
    private String updateTimeTo;
    @JsonProperty("item_status")
    private String itemStatus;
}
