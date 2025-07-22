package com.example.demo.product.internal;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShopeeProductResponse {
    private String error;
    private String message;
    private String warning;
    private String request_id;
    private ResponseData response;

    // Getters and Setters

    @Getter
    @Setter
    public static class ResponseData {
        private Item[] item;
        private Tag tag;

    }

    @Setter
    @Getter
    public static class Item {
        private long item_id;
        private String item_status;
        private long update_time;

        // Getters and Setters

    }

    @Setter
    @Getter
    public static class Tag {
        private long total_count;
        private boolean has_next_page;
        private long next_offset;

        // Getters and Setters

    }
}