package com.backend.foodweb.user;

import com.backend.foodweb.firebase.DataBaseReference;
import com.backend.foodweb.firebase.FirebaseService;
import com.backend.foodweb.merchant.CreateMerchantDTO;
import com.backend.foodweb.merchant.FoodItemDTO;
import com.backend.foodweb.merchant.MerchantLoginDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
        // Retrieve user or merchant from the database based on the provided email
        System.out.println("Login attempt - Email: " + loginDTO.getEmail() + ", Password: " + loginDTO.getHashedpassword());

        // Retrieve user details
        CreateUserDTO user = getUserByEmail(loginDTO.getEmail());

        // Retrieve merchant details
        CreateMerchantDTO merchant = getMerchantByEmail(loginDTO.getEmail());
        System.out.println("Retrieved merchant: " + merchant);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // Check if either user or merchant is found
        if (user != null) {
            // Check if the provided password matches the stored hashed password for the user
            if (passwordEncoder.matches(loginDTO.getHashedpassword(), user.getHashedpassword())) {
                System.out.println("User Login successful");
                // Redirect to user page
                return ResponseEntity.ok("User Login successful - Redirect to user page");
            } else {
                System.out.println("Invalid password for user");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password for user");
            }
        } else if (merchant != null) {
            // Check if the provided password matches the stored hashed password for the merchant
            if (passwordEncoder.matches(loginDTO.getHashedpassword(), merchant.getHashedpassword())) {
                System.out.println("Merchant Login successful");
                // Redirect to merchant page
                return ResponseEntity.ok("Merchant Login successful - Redirect to merchant page");
            } else {
                System.out.println("Invalid password for merchant");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password for merchant");
            }
        } else {
            System.out.println("User or Merchant not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or Merchant not found");
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


}
