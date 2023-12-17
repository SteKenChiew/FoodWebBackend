package com.backend.foodweb.cart;

import com.backend.foodweb.merchant.CreateMerchantDTO;
import com.backend.foodweb.merchant.FoodItemDTO;
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

    public Order createOrder(List<CartItemDTO> cartItems) {
        Order order = new Order();
        order.setCartItems(cartItems);
        order.setStatus("active"); // You can set the initial status as needed
        return order;
    }
    public void clearCart(CartDTO cart) {
        // Clear the cart items list
        if (cart != null && cart.getCartItems() != null) {
            cart.getCartItems().clear();
        }



        // You can also log or print a message to indicate that the cart has been cleared
        System.out.println("Cart cleared: " + cart);
    }


}
