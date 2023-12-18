// CreateAdminDTO.java
package com.backend.foodweb.admin;

import lombok.Data;

@Data
public class CreateAdminDTO {
    private String email;
    private String password; // You might want to hash this in a real-world scenario

    // constructors, getters, and setters

    public CreateAdminDTO() {
        // Default constructor
    }

    public CreateAdminDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
