package com.backend.foodweb.cart;

import com.backend.foodweb.firebase.DataBaseReference;
import com.backend.foodweb.firebase.FirebaseService;
import com.backend.foodweb.merchant.FoodItemDTO;
import com.backend.foodweb.user.CreateUserDTO;
import com.backend.foodweb.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class CartController {
    @Autowired
    private FirebaseService firebaseService;
    @Autowired
    private CartService cartService;
    @Autowired
    private UserService userService;


    @PostMapping("user/cart/add")
    public ResponseEntity<Void> addToCart(
            @RequestParam String uuid,
            @RequestBody Map<String, Object> requestMap
    ) {
        CreateUserDTO userDTO = userService.getUserById(uuid);
        System.out.println("Called ");
        System.out.println("Received cartItem: " + requestMap);

        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Extract the necessary fields from the requestMap
        int itemID = ((Number) requestMap.get("itemID")).intValue();
        int quantity = ((Number) requestMap.get("quantity")).intValue();

        // Check if the item already exists in the user's cart
        Optional<CartItemDTO> existingCartItem = userDTO.getCart().getCartItems().stream()
                .filter(item -> item.getFoodItem().getItemID() == itemID)
                .findFirst();

        if (existingCartItem.isPresent()) {
            // If the item already exists, update the quantity
            existingCartItem.get().setQuantity(existingCartItem.get().getQuantity() + quantity);
        } else {
            // If the item does not exist, create a new CartItemDTO
            String itemCategory = (String) requestMap.get("itemCategory");
            String itemDescription = (String) requestMap.get("itemDescription");
            String itemImg = (String) requestMap.get("itemImg");
            String itemName = (String) requestMap.get("itemName");
            double itemPrice = ((Number) requestMap.get("itemPrice")).doubleValue();
            int itemTotalSale = ((Number) requestMap.get("itemTotalSale")).intValue();

            CartItemDTO cartItem = new CartItemDTO();
            FoodItemDTO foodItem = new FoodItemDTO();
            foodItem.setItemID(itemID);
            foodItem.setItemCategory(itemCategory);
            foodItem.setItemDescription(itemDescription);
            foodItem.setItemImg(itemImg);
            foodItem.setItemName(itemName);
            foodItem.setItemPrice(itemPrice);
            foodItem.setItemTotalSale(itemTotalSale);

            cartItem.setFoodItem(foodItem);
            cartItem.setQuantity(quantity);
            userDTO.getCart().getCartItems().add(cartItem);
        }

        // Save the updated userDTO back to the database
        firebaseService.writeToFirebase(DataBaseReference.USER, userDTO);

        return ResponseEntity.ok().build();
    }



    private String extractUserIdFromRequest(HttpServletRequest request) {
        // Logic to extract the user ID from request headers or wherever it is stored
        // Example: request.getHeader("userId")
        return "exampleUserId";
    }



    @GetMapping("user/cart")
    public ResponseEntity<CartDTO> getCart(@RequestParam String uuid) {
        CreateUserDTO userDTO = userService.getUserById(uuid);
        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        CartDTO cartDTO = userDTO.getCart();
        if (cartDTO == null) {
            return ResponseEntity.ok(new CartDTO()); // Return an empty cart if it's null
        }

        return ResponseEntity.ok(cartDTO);
    }

    // Other endpoints for updating quantities, removing items, etc.
}