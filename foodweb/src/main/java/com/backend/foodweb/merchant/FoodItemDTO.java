package com.backend.foodweb.merchant;
import com.google.type.Decimal;
import lombok.Data;
@Data
public class FoodItemDTO {

    private String itemName;
    private String itemImg;
    private Double itemPrice;
    private String itemCategory;
    private String itemDescription;
    private int itemTotalSale;
}
