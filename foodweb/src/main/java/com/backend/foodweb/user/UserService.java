package com.backend.foodweb.user;

import com.backend.foodweb.cart.CartItemDTO;
import com.backend.foodweb.firebase.DataBaseReference;
import com.backend.foodweb.firebase.FirebaseService;
import com.backend.foodweb.merchant.CreateMerchantDTO;
import com.backend.foodweb.merchant.FoodItemDTO;
import com.backend.foodweb.merchant.MerchantLoginDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.rpc.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.*;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UserService {

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(String username) {
        // Set the expiration time for the token (e.g., 1 hour)
        long expirationTimeMillis = System.currentTimeMillis() + 3600000; // 1 hour

        // Build the JWT token
        String token = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(expirationTimeMillis))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        return token;
    }
    public ResponseEntity createUser(CreateUserDTO createUserDTO) {
        try {
            UUID uuid = UUID.randomUUID();
            createUserDTO.setUUID(uuid.toString());

            String hashedPassword = hashPassword(createUserDTO.getHashedpassword());
            createUserDTO.setHashedpassword(hashedPassword);

            // Generate and set the token (assuming you have a method to generate tokens)
            String token = generateToken(createUserDTO.getUsername());
            createUserDTO.setToken(token);

            // Convert CreateUserDTO to JSON string
            String createUserString = objectMapper.writeValueAsString(createUserDTO);

            // Write to Firebase
            firebaseService.writeToFirebase(DataBaseReference.USER, createUserDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(createUserDTO);
        } catch (JsonProcessingException e) {
            // Handle the exception appropriately (e.g., log it or return an error response)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    public ResponseEntity login(LoginDTO loginDTO) {
        System.out.println("Login attempt - Email: " + loginDTO.getEmail() + ", Password: " + loginDTO.getHashedpassword());

        CreateUserDTO user = getUserByEmail(loginDTO.getEmail());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (user != null) {
            System.out.println("Provided hashed password: " + loginDTO.getHashedpassword());
            System.out.println("Stored hashed password: " + user.getHashedpassword());

            if (passwordEncoder.matches(loginDTO.getHashedpassword(), user.getHashedpassword())) {
                System.out.println("Password matches");
                return ResponseEntity.ok(user);
            } else {
                System.out.println("Password does not match");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
            }
        } else {
            System.out.println("User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }




    public CreateUserDTO getUserByEmail(String email) {
        return firebaseService.getObjectByEmail(email, DataBaseReference.USER, CreateUserDTO.class);
    }

    public CreateMerchantDTO getMerchantByEmail(String email) {
        System.out.println("Querying for email: " + email);

        CreateMerchantDTO merchant = firebaseService.getObjectByEmail(email, DataBaseReference.MERCHANT, CreateMerchantDTO.class);
        System.out.println("Retrieved merchant after latch: " + merchant);
        return merchant;
    }

    private String hashPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    public ResponseEntity createMerchant(CreateMerchantDTO merchantDTO) {
        try {
            UUID uuid = UUID.randomUUID();
            merchantDTO.setUUID(uuid.toString());

            String hashedPassword = hashPassword(merchantDTO.getHashedpassword());
            merchantDTO.setHashedpassword(hashedPassword);

            String token = generateToken(merchantDTO.getMerchantName());
            merchantDTO.setToken(token);


            // Convert MerchantDTO to JSON string
            String createMerchantString = objectMapper.writeValueAsString(merchantDTO);

            System.out.println("MerchantDTO before writing to the database: " + merchantDTO);

            // Write to Firebase
            firebaseService.writeToFirebaseMerchant(DataBaseReference.MERCHANT, merchantDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(merchantDTO);
        } catch (JsonProcessingException e) {
            // Handle the exception appropriately
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    public ResponseEntity merchantLogin(MerchantLoginDTO merchantLoginDTO) {
        // Retrieve merchant details
        CreateMerchantDTO merchant = getMerchantByEmail(merchantLoginDTO.getEmail());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Check if the merchant is found
        if (merchant != null) {
            // Check if the provided password matches the stored hashed password for the merchant
            if (passwordEncoder.matches(merchantLoginDTO.getHashedpassword(), merchant.getHashedpassword())) {
                // Merchant Login successful
                return ResponseEntity.ok("Merchant Login successful - Redirect to merchant page");
            } else {
                // Invalid password for merchant
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password for merchant");
            }
        } else {
            // Merchant not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Merchant not found");
        }
    }


        public CreateMerchantDTO getMerchantByUUID(String uuid) {
            try {
                // Retrieve the list of all merchants from Firebase
                List<CreateMerchantDTO> allMerchants = firebaseService.getMerchantsFromFirebase();

                // Filter the list to find the merchant with the specified UUID
                Optional<CreateMerchantDTO> matchingMerchant = allMerchants.stream()
                        .filter(merchant -> merchant.getUUID().equals(uuid))
                        .findFirst();

                // Return the matching merchant if found, otherwise return null
                return matchingMerchant.orElse(null);
            } catch (Exception e) {
                // Handle the exception appropriately
                throw new RuntimeException("Error fetching merchant by UUID", e);
            }
        }

    public ResponseEntity<Map<String, String>> uploadImage(MultipartFile file) {
        try {
            // Specify the path where you want to save the uploaded images
            String uploadDir = "C:\\GitHub\\FoodWeb\\src\\assets\\img";
            Path uploadPath = Paths.get(uploadDir);

            // If the directory doesn't exist, create it
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Get the original filename
            String originalFileName = file.getOriginalFilename();

            // Build the path where the file will be saved
            Path filePath = uploadPath.resolve(originalFileName);

            // Save the file to the specified path
            Files.write(filePath, file.getBytes());

            // Construct a JSON response with the file path
            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("path", filePath.toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Handle the exception appropriately (e.g., log it or return an error response)
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    public ResponseEntity addToCart(String userId, FoodItemDTO foodItem, int quantity) {
        CreateUserDTO userDTO = getUserById(userId);

        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Update the user's cart
        CartItemDTO cartItem = new CartItemDTO();
        cartItem.setFoodItem(foodItem);
        cartItem.setQuantity(quantity);

        userDTO.getCart().getCartItems().add(cartItem);

        // Save the updated userDTO back to the database
        firebaseService.writeToFirebase(DataBaseReference.USER, userDTO);

        return ResponseEntity.ok("Item added to cart successfully");
    }

    public CreateUserDTO getUserById(String userId) {
        return firebaseService.readFromFirebase(DataBaseReference.USER, userId, CreateUserDTO.class);
    }


}



