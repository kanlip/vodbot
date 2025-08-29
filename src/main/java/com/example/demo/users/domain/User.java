package com.example.demo.users.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    // Getters and setters
    private String id;
    private String orgId;
    private String email;
    private String passwordHash;
    private List<String> roles;
    private String displayName;
    private boolean isSupervisor;
    private String supervisorPinHash;
    private Date lastLoginAt;
    private String status;
    private Date createdAt;
    private Date updatedAt;


}

