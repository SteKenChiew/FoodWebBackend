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

    private String hashPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }
}
