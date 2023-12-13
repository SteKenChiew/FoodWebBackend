package com.backend.foodweb.user;

import com.backend.foodweb.cart.CartDTO;
import com.backend.foodweb.cart.Order;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateUserDTO {
    private String username;
    private String email;
    private String hashedpassword;
    private String token;
    private String UUID;
    private CartDTO cart;
    private List<Order> activeOrders;
    private List<Order> orderHistory;
    public CreateUserDTO() {
        this.cart = new CartDTO();
    }
    public List<Order> getActiveOrders() {
        if (activeOrders == null) {
            activeOrders = new ArrayList<>();
        }
        return activeOrders;
    }
    public void setToken(String token) {
        this.token = token;
    }
}
