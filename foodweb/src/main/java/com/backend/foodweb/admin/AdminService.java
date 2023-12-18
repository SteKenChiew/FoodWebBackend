// AdminService.java
package com.backend.foodweb.admin;

import com.backend.foodweb.firebase.DataBaseReference;
import com.backend.foodweb.firebase.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private FirebaseService firebaseService;

    public ResponseEntity<AdminDTO> authenticateAdmin(AdminLoginDTO adminLoginDTO) {
        // Retrieve the admin from Firebase based on the provided email
        AdminDTO storedAdmin = firebaseService.getAdminByEmail(adminLoginDTO.getEmail());

        if (storedAdmin != null && BCrypt.checkpw(adminLoginDTO.getPassword(), storedAdmin.getPassword())) {
            // Authentication successful
            return ResponseEntity.ok(storedAdmin);
        } else {
            // Authentication failed
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }


    public ResponseEntity registerAdmin(AdminDTO adminDTO) {
        // Hash the admin's password
        String hashedPassword = BCrypt.hashpw(adminDTO.getPassword(), BCrypt.gensalt());

        // Create a new admin with the hashed password
        CreateAdminDTO createAdminDTO = new CreateAdminDTO(adminDTO.getEmail(), hashedPassword);

        // Call the writeToFirebaseAdmin method in FirebaseService
        firebaseService.writeToFirebaseAdmin(DataBaseReference.ADMIN, createAdminDTO);

        return ResponseEntity.ok("Admin registration successful");
    }
}
