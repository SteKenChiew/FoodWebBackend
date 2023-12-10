package com.backend.foodweb.merchant;
import lombok.Data;

@Data
public class CreateMerchantDTO {

    private String merchantName;
    private String merchantImage;
    private String merchantEmail;
    private String hashedpassword;
    private String merchantType;
    private String token;
    private String UUID;

    public CreateMerchantDTO() {}
}
