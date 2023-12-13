package com.backend.foodweb.user;

import com.backend.foodweb.cart.CartDTO;
import lombok.Data;
import lombok.Setter;
@Data
public class CreateUserDTO {
    private String username;
    private String email;
    private String hashedpassword;
    private String token;
    private String UUID;
    private CartDTO cart;
    public CreateUserDTO() {
        this.cart = new CartDTO();
    }

    public void setToken(String token) {
        this.token = token;
    }
}
