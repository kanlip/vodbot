package com.example.demo.user.entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "users")
public class UserEntity {
    @Id
    private ObjectId id;

    private String owner;
    private String password;
    private Role roles;


    // Other fields as needed
    public enum Role {
        MANAGER,
        PACKER
    }

}



