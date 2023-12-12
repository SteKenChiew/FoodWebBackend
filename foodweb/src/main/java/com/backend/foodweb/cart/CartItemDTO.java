package com.backend.foodweb.cart;

import com.backend.foodweb.merchant.FoodItemDTO;
import lombok.Data;

@Data
public class CartItemDTO {
    private FoodItemDTO foodItem;
    private int quantity;

    // Other fields and methods as needed
}
