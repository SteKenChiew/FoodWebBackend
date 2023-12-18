package com.backend.foodweb.admin;

import lombok.Data;

@Data
public class AdminDTO {
    private String email;
    private String password;
    private  String username;
    private  int id;

    public AdminDTO() {}

    // constructor with fields
    public AdminDTO(String email, String password, String username, int id) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.id = id;
    }
}
