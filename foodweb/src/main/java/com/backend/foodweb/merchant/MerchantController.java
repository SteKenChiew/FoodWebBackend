package com.backend.foodweb.merchant;

import com.backend.foodweb.JwtUtils;
import com.backend.foodweb.cart.Order;
import com.backend.foodweb.firebase.FirebaseService;
import com.backend.foodweb.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MerchantController {

    @Autowired
    UserService userService;
    @Autowired
    private FirebaseService firebaseService;

    @GetMapping("merchant/orders/active")
    public ResponseEntity<List<Order>> getMerchantActiveOrders(@RequestParam String merchantUuid) {
        // Retrieve merchantDTO from the service
        CreateMerchantDTO merchantDTO = userService.getMerchantByUUID(merchantUuid);
        System.out.println("Hel");
        // Check if merchantDTO is null
        if (merchantDTO == null) {
            // Handle the case when merchantDTO is null
            // Log an error or throw an exception, depending on your requirements
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Get the merchant's active orders
        List<Order> activeOrders = merchantDTO.getActiveOrders();

        return ResponseEntity.ok(activeOrders);
    }
}
