package com.backend.foodweb.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private List<CartItemDTO> cartItems = new ArrayList<>();

    public void addToCart(CartItemDTO cartItemDTO) {
        // Check if the item is already in the cart
        Optional<CartItemDTO> existingItem = cartItems.stream()
                .filter(item -> item.getFoodItem().getItemID() == cartItemDTO.getFoodItem().getItemID())
                .findFirst();

        if (existingItem.isPresent()) {
            // If it exists, update the quantity
            existingItem.get().setQuantity(existingItem.get().getQuantity() + cartItemDTO.getQuantity());
        } else {
            // If it doesn't exist, add the item to the cart
            cartItems.add(cartItemDTO);
        }
    }

    public List<CartItemDTO> getCartItems() {
        return cartItems;
    }

    // Other methods for updating quantities, removing items, etc.
}
