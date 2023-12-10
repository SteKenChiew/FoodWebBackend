package com.backend.foodweb.merchant;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
    public CreateMerchantDTO() {
        this.foodItems = new ArrayList<>();
    }
}
