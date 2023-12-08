package com.backend.foodweb.user;

import lombok.Data;

@Data
public class LoginDTO {
    private String email;
    private String hashedpassword;
}
