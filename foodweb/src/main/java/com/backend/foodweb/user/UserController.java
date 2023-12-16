package com.backend.foodweb.user;

import com.backend.foodweb.JwtUtils;
import com.backend.foodweb.cart.Order;
import com.backend.foodweb.firebase.DataBaseReference;
import com.backend.foodweb.firebase.FirebaseInitializer;
import com.backend.foodweb.firebase.FirebaseService;
import com.backend.foodweb.merchant.CreateMerchantDTO;
import com.backend.foodweb.merchant.FoodItemDTO;
import com.backend.foodweb.merchant.MerchantLoginDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    private FirebaseService firebaseService;
    @Autowired
    private FirebaseInitializer.FirebaseStorageService firebaseStorageService;
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


    @PostMapping(value = "/merchant/add-item")
    public ResponseEntity addFoodItemToMerchant(
            @RequestParam String image,  // Use String instead of MultipartFile
            @RequestParam String merchantEmail,
            @RequestParam("foodItem") String foodItemJson) {
        try {
            // Log relevant information
            System.out.println("Merchant Email: " + merchantEmail);

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

            // Deserialize foodItemJson to FoodItemDTO
            ObjectMapper objectMapper = new ObjectMapper();
            FoodItemDTO foodItemDTO = objectMapper.readValue(foodItemJson, FoodItemDTO.class);

            // Set the itemID for the new food item
            foodItemDTO.setItemID(getNextItemID(merchant.getFoodItems()));

            // Set the image URL in the food item
            foodItemDTO.setItemImg(image);

            // Add the new food item to the merchant's foodItems list
            merchant.getFoodItems().add(foodItemDTO);

            // Update the merchant in the database
            firebaseService.writeToFirebaseMerchant(DataBaseReference.MERCHANT, merchant);

            return ResponseEntity.ok("Item added successfully");
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception details
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }


    // Method to save the image to a local directory
//    private String saveImageLocally(MultipartFile image, String merchantEmail, String itemId) throws IOException {
//        // Define the directory to save images
//        String uploadDirectory = "C:\\Users\\chiew\\Desktop\\Testingimg\\";
//
//        // Generate a unique filename for the image
//        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
//
//        // Construct the complete file path
//        String filePath = uploadDirectory + merchantEmail + "\\" + itemId + "\\" + fileName;
//
//        // Create directories if they don't exist
//        Files.createDirectories(Paths.get(filePath).getParent());
//
//        // Save the image to the specified path
//        Files.write(Paths.get(filePath), image.getBytes());
//
//        // Return the URL or relative path to the saved image
//        return "/images/" + merchantEmail + "/" + itemId + "/" + fileName;
//    }


    // Helper method to get the next available itemID
    private int getNextItemID(List<FoodItemDTO> foodItems) {
        int maxID = foodItems.stream().mapToInt(FoodItemDTO::getItemID).max().orElse(0);
        return maxID + 1;
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


    @PutMapping("/merchant/update-food-item")
    public ResponseEntity<?> updateFoodItem(@RequestBody FoodItemDTO updatedFoodItem,
                                            @RequestParam String merchantEmail,
                                            @RequestParam int itemID) {
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

            // Find the index of the food item to be updated based on its itemID
            int index = findFoodItemIndexByItemID(merchant.getFoodItems(), itemID);

            // If the food item is not found, return a not found response
            if (index == -1) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Food item not found");
            }

            // Update the food item at the found index
            merchant.getFoodItems().set(index, updatedFoodItem);

            // Update the merchant in the database
            firebaseService.writeToFirebaseMerchant(DataBaseReference.MERCHANT, merchant);

            return ResponseEntity.ok(merchant);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            // Handle the exception appropriately and provide a meaningful response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    // Helper method to find the index of a food item in the list based on its itemID
    private int findFoodItemIndexByItemID(List<FoodItemDTO> foodItems, int itemID) {
        for (int i = 0; i < foodItems.size(); i++) {
            if (foodItems.get(i).getItemID() == itemID) {
                return i;
            }
        }
        return -1;
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<CreateMerchantDTO>> getRestaurants() {
        try {
            // Retrieve the list of restaurants from Firebase
            List<CreateMerchantDTO> restaurants = firebaseService.getMerchantsFromFirebase();

            return ResponseEntity.ok(restaurants);
        } catch (Exception e) {
            // Handle the exception appropriately
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/restaurants/{uuid}")
    public ResponseEntity<?> getRestaurantByUUID(@PathVariable String uuid) {
        try {
            // Retrieve the merchant with the specified UUID from the service
            CreateMerchantDTO merchant = userService.getMerchantByUUID(uuid);

            // Check if a matching merchant is found
            if (merchant != null) {
                return ResponseEntity.ok(merchant);
            } else {
                // If no matching merchant is found, return a 404 Not Found status
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            // Handle the exception appropriately
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/api/upload-image")
    public ResponseEntity<Map<String, String>> handleFileUpload(@RequestParam("file") MultipartFile file) {
        return userService.uploadImage(file);
    }

    @GetMapping("/user/order-summary/{uuid}/{bookingId}")
    public ResponseEntity<Order> getOrderSummary(
            @PathVariable String uuid,
            @PathVariable String bookingId) {
        System.out.println("Test");
        CreateUserDTO userDTO = userService.getUserById(uuid);

        if (userDTO != null) {
            List<Order> activeOrders = userDTO.getActiveOrders();

            // Find the order with the given bookingId
            Optional<Order> foundOrder = activeOrders.stream()
                    .filter(order -> bookingId.equals(order.getBookingId()))
                    .findFirst();

            if (foundOrder.isPresent()) {
                return ResponseEntity.ok(foundOrder.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }



}





