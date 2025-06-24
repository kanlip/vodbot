package com.example.demo.order.entity;


import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Package {

    String id;
    @Indexed
    String package_id;

    String packer_id;

    
}
