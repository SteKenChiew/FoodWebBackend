// CreateAdminDTO.java
package com.backend.foodweb.admin;

import lombok.Data;

@Data
public class CreateAdminDTO {
    private String email;
    private String password; // You might want to hash this in a real-world scenario
    private  String username;
    private  int id;
    // constructors, getters, and setters

    public CreateAdminDTO() {
        // Default constructor
    }

    public CreateAdminDTO(String email, String password, String username, int id) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.id = id;
    }
}
