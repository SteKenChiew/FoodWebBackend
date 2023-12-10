package com.backend.foodweb.user;

import com.backend.foodweb.JwtUtils;
import com.backend.foodweb.merchant.CreateMerchantDTO;
import com.backend.foodweb.merchant.FoodItemDTO;
import com.backend.foodweb.merchant.MerchantLoginDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    UserService userService;

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
        List<FoodItemDTO> foodItems = merchantDTO.getFoodItems();
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
}

