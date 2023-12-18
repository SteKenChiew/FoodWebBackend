package com.backend.foodweb.admin;

import lombok.Data;

@Data
public class AdminDTO {
    private String email;
    private String password;

    // getters and setters

    // constructor
    public AdminDTO() {}

    // constructor with fields
    public AdminDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
