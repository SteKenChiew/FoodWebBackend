package com.backend.foodweb.user;

import com.backend.foodweb.firebase.DataBaseReference;
import com.backend.foodweb.firebase.FirebaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private ObjectMapper objectMapper;

    public ResponseEntity createUser(CreateUserDTO createUserDTO) {
        try {
            UUID uuid = UUID.randomUUID();
            createUserDTO.setUUID(uuid.toString());

            String hashedPassword = hashPassword(createUserDTO.getHashedpassword());
            createUserDTO.setHashedpassword(hashedPassword);

            String createUserString = objectMapper.writeValueAsString(createUserDTO);
            firebaseService.writeToFirebase(DataBaseReference.USER, createUserString, uuid.toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(createUserDTO);
        } catch (JsonProcessingException e) {
            // Handle the exception appropriately (e.g., log it or return an error response)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity login(String email, String password) {
        // Retrieve user from the database based on the provided email
        // You might need to implement a method to fetch user details from the database
        System.out.println("Login attempt - Email: " + email + ", Password: " + password);

        // For illustration purposes, let's assume you have a method like getUserByEmail
        // Implement this method in your FirebaseService or another service class
        CreateUserDTO user = firebaseService.getUserByEmail(email);

        if (user != null) {
            // Check if the provided password matches the stored hashed password
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(password, user.getHashedpassword())) {
                System.out.println("Login successful");
                return ResponseEntity.ok("Login successful");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }


    private String hashPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }
}
