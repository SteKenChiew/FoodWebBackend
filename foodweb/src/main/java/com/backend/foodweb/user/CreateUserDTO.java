package com.backend.foodweb.user;

import lombok.Data;
import lombok.Setter;
@Data
public class CreateUserDTO {
    private String username;
    private String email;
    private String hashedpassword;
    private String token;
    private String UUID;
    public CreateUserDTO() {}

    public void setToken(String token) {
        this.token = token;
    }
}
