// AdminController.java
package com.backend.foodweb.admin;

import com.backend.foodweb.firebase.FirebaseService;
import com.backend.foodweb.user.CreateUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class AdminController {
    @Autowired
    private FirebaseService firebaseService;
    @Autowired
    private AdminService adminService;

    @PostMapping("/admin/login")
    public ResponseEntity loginAdmin(@RequestBody AdminLoginDTO adminLoginDTO) {
        return adminService.authenticateAdmin(adminLoginDTO);
    }

    @PostMapping("/admin/register")
    public ResponseEntity registerAdmin(@RequestBody AdminDTO adminDTO) {
        return adminService.registerAdmin(adminDTO);
    }

    @GetMapping("admin/user-list")
    public ResponseEntity<List<CreateUserDTO>> getUserList() {
        try {
            // Retrieve the list of restaurants from Firebase
            List<CreateUserDTO> user = firebaseService.getUserFromFirebase();

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            // Handle the exception appropriately
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
