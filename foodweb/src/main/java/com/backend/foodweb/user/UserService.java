package com.backend.foodweb.user;

import com.backend.foodweb.firebase.DataBaseReference;
import com.backend.foodweb.firebase.FirebaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    FirebaseService firebaseService;

    public ResponseEntity createUser(CreateUserDTO createUserDTO) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        UUID uuid = UUID.randomUUID();
        createUserDTO.setUUID(uuid.toString());
        String createUserString = mapper.writeValueAsString(createUserDTO);
        firebaseService.writeToFirebase(DataBaseReference.USER, createUserString, uuid.toString());

        return ResponseEntity.ok(createUserDTO);
    }
}
