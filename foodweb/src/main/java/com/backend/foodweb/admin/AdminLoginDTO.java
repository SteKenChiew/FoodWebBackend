package com.backend.foodweb.admin;

import lombok.Data;

@Data
// AdminLoginDTO.java
public class AdminLoginDTO {
    private String email;
    private String password;

    // getters and setters

    // constructor
    public AdminLoginDTO() {}

    // constructor with fields
    public AdminLoginDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
