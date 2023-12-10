package com.backend.foodweb.user;

import com.backend.foodweb.JwtUtils;
import com.backend.foodweb.firebase.DataBaseReference;
import com.backend.foodweb.firebase.FirebaseService;
import com.backend.foodweb.merchant.CreateMerchantDTO;
import com.backend.foodweb.merchant.FoodItemDTO;
import com.backend.foodweb.merchant.MerchantLoginDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    private FirebaseService firebaseService;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/user/create")
    public ResponseEntity createUser(@RequestBody CreateUserDTO createUserDTO) {
        ResponseEntity responseEntity = userService.createUser(createUserDTO);

        // Generate JWT token and add it to the response if the user is created successfully
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String token = jwtUtils.generateToken(createUserDTO.getUsername());
            createUserDTO.setToken(token);  // Set token in the user DTO

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(createUserDTO);  // Include user DTO with token in the response body
        }

        return responseEntity;
    }

    @PostMapping("/user/login")
    public ResponseEntity login(@RequestBody LoginDTO loginDTO) {
        // Validate and process login
        ResponseEntity responseEntity = userService.login(loginDTO);

        // If login is successful, generate JWT token and add it to the response
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            // Get the user details
            CreateUserDTO userDTO = userService.getUserByEmail(loginDTO.getEmail());

            // Generate JWT token
            String token = jwtUtils.generateToken(userDTO.getUsername());

            // Set token in the user DTO
            userDTO.setToken(token);

            // Add the token to the response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);

            // Add any other headers you need
            headers.add("Access-Control-Expose-Headers", "Authorization");

            // Return the response with headers and user DTO
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(userDTO);
        }

        return responseEntity;
    }


    @PostMapping("/merchant/create")
    public ResponseEntity createMerchant(@RequestBody CreateMerchantDTO merchantDTO) {

        ResponseEntity responseEntity = userService.createMerchant(merchantDTO);

        // Generate JWT token and add it to the response if the merchant is created successfully
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String token = jwtUtils.generateToken(merchantDTO.getMerchantName());
            merchantDTO.setToken(token);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);
            System.out.println("Received request to create merchant: " + merchantDTO);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(merchantDTO);
        }

        return responseEntity;
    }

    @PostMapping("/merchant/login")
    public ResponseEntity merchantLogin(@RequestBody MerchantLoginDTO merchantLoginDTO) {
        System.out.println("Login attempt - Email: " + merchantLoginDTO.getEmail() + ", Password: " + merchantLoginDTO.getHashedpassword());

        ResponseEntity responseEntity = userService.merchantLogin(merchantLoginDTO);

        // If login is successful, generate JWT token and add it to the response
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            // Get the merchant details
            CreateMerchantDTO merchantDTO = userService.getMerchantByEmail(merchantLoginDTO.getEmail());

            // Generate JWT token
            String token = jwtUtils.generateToken(merchantDTO.getMerchantName());

            // Set token in the merchant DTO
            merchantDTO.setToken(token);

            // Add the token to the response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);

            // Add any other headers you need
            headers.add("Access-Control-Expose-Headers", "Authorization");

            // Return the response with headers and merchant DTO
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(merchantDTO);
        }

        return responseEntity;
    }


    @PostMapping("/merchant/add-item")
    public ResponseEntity addFoodItemToMerchant(@RequestBody FoodItemDTO foodItemDTO, @RequestParam String merchantEmail) {
        try {
            // Retrieve the merchant by email
            CreateMerchantDTO merchant = userService.getMerchantByEmail(merchantEmail);

            // Check if the merchant exists
            if (merchant == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Merchant not found");
            }

            // Ensure that foodItems is initialized as a List
            if (merchant.getFoodItems() == null) {
                merchant.setFoodItems(new ArrayList<>());
            }

            // Add the new food item to the merchant's foodItems list
            merchant.getFoodItems().add(foodItemDTO);

            // Update the merchant in the database
            firebaseService.writeToFirebaseMerchant(DataBaseReference.MERCHANT, merchant);

            return ResponseEntity.status(HttpStatus.CREATED).body(merchant);
        } catch (Exception e) {
            // Handle the exception appropriately
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/merchant/get-food-items")
    public ResponseEntity<List<FoodItemDTO>> getFoodItems(@RequestParam String merchantEmail) {
        try {
            // Retrieve the merchant by email
            CreateMerchantDTO merchant = userService.getMerchantByEmail(merchantEmail);

            // Check if the merchant exists
            if (merchant == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Get the food items from the merchant
            List<FoodItemDTO> foodItems = merchant.getFoodItems();

            return ResponseEntity.ok(foodItems);
        } catch (Exception e) {
            // Handle the exception appropriately
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}



