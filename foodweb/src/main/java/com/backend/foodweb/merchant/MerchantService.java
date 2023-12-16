package com.backend.foodweb.merchant;

import com.backend.foodweb.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantService {


    @Autowired
    private UserService userService;

    public void updateItemAvailability(String merchantEmail, int itemID, boolean itemAvailability) {
        // Fetch the merchant by email using UserService
        CreateMerchantDTO merchant = userService.getMerchantByEmail(merchantEmail);

        // Find and update the item in the merchant's food items list
        if (merchant != null && merchant.getFoodItems() != null) {
            List<FoodItemDTO> foodItems = merchant.getFoodItems();
            for (FoodItemDTO foodItem : foodItems) {
                if (foodItem.getItemID() == itemID) {
                    foodItem.setItemAvailability(itemAvailability);
                    break;
                }
            }

            // Update the merchant in the database
            userService.updateMerchant(merchant);
        }
    }



}

