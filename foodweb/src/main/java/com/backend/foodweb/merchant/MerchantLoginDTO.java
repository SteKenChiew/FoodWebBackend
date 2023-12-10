package com.backend.foodweb.merchant;
import lombok.Data;

@Data
public class MerchantLoginDTO {
    private String email;
    private String hashedpassword;
}
