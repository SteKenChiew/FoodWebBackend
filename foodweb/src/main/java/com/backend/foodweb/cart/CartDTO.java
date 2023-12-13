package com.backend.foodweb.cart;

import lombok.Data;

import java.util.List;

@Data
public class CartDTO {
    private List<CartItemDTO> cartItems;
    private String MerchantUUID;
    // Getter method for cartItems
    public List<CartItemDTO> getCartItems() {
        return cartItems;
    }

    public String getMerchantUUID() {
        return MerchantUUID;
    }

}
