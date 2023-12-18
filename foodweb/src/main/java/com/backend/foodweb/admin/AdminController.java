// AdminController.java
package com.backend.foodweb.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminController {

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
}
