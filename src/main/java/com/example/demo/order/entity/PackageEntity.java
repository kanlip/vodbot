package com.example.demo.order.entity;


import com.example.demo.user.entity.UserEntity;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Builder
@Data
@Document
public class PackageEntity {

    @Id
    ObjectId id;
    @Indexed
    String package_id;

    // Ensure the type is correctly referenced to UserEntity
    ObjectId packerId;
}


