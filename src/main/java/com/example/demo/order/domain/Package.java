package com.example.demo.order.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Setter
@Getter
public class Package {
    private String id;
    private String packageId;
    private String packerId;
    private List<Item> items;

    @Setter
    @Getter
    public static class Item {
        private String sku;
        private Integer quantity;

    }
}
