package com.backend.foodweb.user;

import lombok.Data;

@Data
public class CreateUserDTO {
    private String UUID;
    private String username;
    private String email;
    private String password;
}
