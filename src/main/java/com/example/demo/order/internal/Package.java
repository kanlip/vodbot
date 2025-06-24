package com.example.demo.order.internal;


import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document
public class Package {

    @Id
    ObjectId id;
    @Indexed
    String package_id;

    String packer_id;

    
}
