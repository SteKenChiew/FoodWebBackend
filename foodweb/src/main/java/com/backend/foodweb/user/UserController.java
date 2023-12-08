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

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(responseEntity.getBody());
        }

        return responseEntity;
    }

    @PostMapping("/user/login")
    public ResponseEntity login(@RequestBody LoginDTO loginDTO) {
        // Validate and process login
        return userService.login(loginDTO.getEmail(), loginDTO.getHashedpassword());
    }
}

