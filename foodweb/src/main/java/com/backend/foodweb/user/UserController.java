package com.backend.foodweb.user;

import com.backend.foodweb.JwtUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}

