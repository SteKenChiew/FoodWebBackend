package com.backend.foodweb.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping("/user/create")
    public ResponseEntity createUser(@RequestBody CreateUserDTO createUserDTO){
        try{
            return userService.createUser(createUserDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
