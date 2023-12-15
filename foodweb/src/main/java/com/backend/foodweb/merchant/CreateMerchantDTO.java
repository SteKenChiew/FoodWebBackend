package com.backend.foodweb.merchant;
import com.backend.foodweb.cart.Order;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CreateMerchantDTO {
    private String merchantName;
    private String merchantImage;
    private String merchantEmail;
    private String hashedpassword;
    private String merchantType;
    private String token;
    private String UUID;
    private List<FoodItemDTO> foodItems;
    private List<Order> activeOrders;
    private List<Order> readyOrders;
    private List<Order> orderHistory;
    private Double totalSales;
    public CreateMerchantDTO() {
        // Ensure that foodItems is initialized as a Map
        this.foodItems = new ArrayList<>();
    }
    public List<Order> getActiveOrders() {
        if (activeOrders == null) {
            activeOrders = new ArrayList<>();
        }
        return activeOrders;
    }
}
