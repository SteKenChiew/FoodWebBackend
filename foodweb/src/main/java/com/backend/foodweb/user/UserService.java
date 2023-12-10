package com.backend.foodweb.user;

import com.backend.foodweb.firebase.DataBaseReference;
import com.backend.foodweb.firebase.FirebaseService;
import com.backend.foodweb.merchant.CreateMerchantDTO;
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
import java.util.Date;
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
        // Retrieve user from the database based on the provided email
        System.out.println("Login attempt - Email: " + loginDTO.getEmail() + ", Password: " + loginDTO.getHashedpassword());

        // Retrieve user details
        CreateUserDTO user = getUserByEmail(loginDTO.getEmail());
        System.out.println("Retrieved user: " + user);

        // Check if the user is found
        if (user != null) {
            System.out.println("Retrieved user: " + user);
            System.out.println("Provided hashed password: " + loginDTO.getHashedpassword());
            System.out.println("Stored hashed password: " + user.getHashedpassword());

            // Check if the stored hashed password is empty or null
            String storedHashedPassword = user.getHashedpassword();
            if (storedHashedPassword == null || storedHashedPassword.isEmpty()) {
                System.out.println("Stored hashed password is empty or null");
                // Handle this case as needed (return an error or handle it appropriately)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid user data");
            }

            // Check if the provided password matches the stored hashed password
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(loginDTO.getHashedpassword(), storedHashedPassword)) {
                System.out.println("Login successful");
                return ResponseEntity.ok("Login successful");
            } else {
                System.out.println("Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
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
        return firebaseService.getObjectByEmail(email, DataBaseReference.MERCHANT, CreateMerchantDTO.class);
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

            // Write to Firebase
            firebaseService.writeToFirebaseMerchant(DataBaseReference.MERCHANT, merchantDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(merchantDTO);
        } catch (JsonProcessingException e) {
            // Handle the exception appropriately
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
