package com.backend.foodweb.cart;

import com.backend.foodweb.merchant.FoodItemDTO;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class Order {
    private  String merchantName;
    private String orderId;
    private String BookingId;
    private List<CartItemDTO> orderItems;
    private String status; // 'active', 'completed', etc.
    private String orderPlacedDateTime;
    private Double orderTotal;
    public void setCartItems(List<CartItemDTO> cartItems) {
        this.orderItems = cartItems;
    }
}
