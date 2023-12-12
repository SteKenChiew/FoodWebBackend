package com.backend.foodweb.cart;

import lombok.Data;

import java.util.List;

@Data
public class CartDTO {
    private List<CartItemDTO> cartItems;

    // Getter method for cartItems
    public List<CartItemDTO> getCartItems() {
        return cartItems;
    }
}
