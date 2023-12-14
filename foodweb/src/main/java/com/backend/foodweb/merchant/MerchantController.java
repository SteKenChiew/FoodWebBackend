package com.backend.foodweb.merchant;

import com.backend.foodweb.JwtUtils;
import com.backend.foodweb.cart.Order;
import com.backend.foodweb.firebase.FirebaseService;
import com.backend.foodweb.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MerchantController {

    @Autowired
    UserService userService;
    @Autowired
    private FirebaseService firebaseService;

    @GetMapping("merchant/orders/active")
    public ResponseEntity<List<Order>> getMerchantActiveOrders(@RequestParam String merchantUuid) {
        // Retrieve merchantDTO from the service
        CreateMerchantDTO merchantDTO = userService.getMerchantByUUID(merchantUuid);

        // Check if merchantDTO is null
        if (merchantDTO == null) {
            // Handle the case when merchantDTO is null
            // Log an error or throw an exception, depending on your requirements
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Get the merchant's active orders
        List<Order> activeOrders = merchantDTO.getActiveOrders();

        return ResponseEntity.ok(activeOrders);
    }

    @PostMapping("merchant/orders/mark-as-ready")
    public ResponseEntity<Void> moveOrderToReady(
            @RequestParam String merchantUuid,
            @RequestParam String orderId) {

        // Retrieve merchantDTO from the service
        CreateMerchantDTO merchantDTO = userService.getMerchantByUUID(merchantUuid);

        // Check if merchantDTO is null
        if (merchantDTO == null) {
            // Handle the case when merchantDTO is null
            // Log an error or throw an exception, depending on your requirements
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Get the merchant's active orders
        List<Order> activeOrders = merchantDTO.getActiveOrders();

        // Initialize the ready orders list if null
        if (merchantDTO.getReadyOrders() == null) {
            merchantDTO.setReadyOrders(new ArrayList<>());
        }

        // Move the specific order to the ready orders list
        List<Order> readyOrders = merchantDTO.getReadyOrders();

        // Assuming you have a method to find the order by its ID
        Order orderToMove = activeOrders.stream()
                .filter(order -> order.getOrderId().equals(orderId))
                .findFirst()
                .orElse(null);

        if (orderToMove != null) {
            readyOrders.add(orderToMove);
            activeOrders.remove(orderToMove);  // Remove the order from active orders
        } else {
            // Handle the case when the order with the specified ID is not found in active orders
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Update the merchant's information in the database
        userService.updateMerchant(merchantDTO);

        return ResponseEntity.ok().build();
    }
    @GetMapping("merchant/orders/ready")
    public ResponseEntity<List<Order>> getMerchantReadyOrders(@RequestParam String merchantUuid) {
        // Retrieve merchantDTO from the service
        CreateMerchantDTO merchantDTO = userService.getMerchantByUUID(merchantUuid);

        // Check if merchantDTO is null
        if (merchantDTO == null) {
            // Handle the case when merchantDTO is null
            // Log an error or throw an exception, depending on your requirements
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Get the merchant's active orders
        List<Order> readyOrders= merchantDTO.getReadyOrders();

        return ResponseEntity.ok(readyOrders);
    }

    @PostMapping("merchant/orders/mark-as-done")
    public ResponseEntity<Void> moveOrderToDone(
            @RequestParam String merchantUuid,
            @RequestParam String orderId) {

        // Retrieve merchantDTO from the service
        CreateMerchantDTO merchantDTO = userService.getMerchantByUUID(merchantUuid);

        // Check if merchantDTO is null
        if (merchantDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
// Initialize the orderHistory list if null
        if (merchantDTO.getOrderHistory() == null) {
            merchantDTO.setOrderHistory(new ArrayList<>());
        }
        // Get the merchant's ready orders
        List<Order> readyOrders = merchantDTO.getReadyOrders();

        // Find the order in ready orders with the specified ID
        Optional<Order> orderToMove = readyOrders.stream()
                .filter(order -> order.getOrderId().equals(orderId))
                .findFirst();

        if (orderToMove.isPresent()) {
            // Update the order status to "done"
            orderToMove.get().setStatus("done");

            // Move the order to the order history list
            merchantDTO.getOrderHistory().add(orderToMove.get());
            readyOrders.remove(orderToMove.get());

            // Update the merchant's information in the database
            userService.updateMerchant(merchantDTO);

            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @GetMapping("merchant/orders/history")
    public ResponseEntity<List<Order>> getMerchantOrderHistory(@RequestParam String merchantUuid) {
        // Retrieve merchantDTO from the service
        CreateMerchantDTO merchantDTO = userService.getMerchantByUUID(merchantUuid);

        // Check if merchantDTO is null
        if (merchantDTO == null) {
            // Handle the case when merchantDTO is null
            // Log an error or throw an exception, depending on your requirements
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Get the merchant's active orders
        List<Order> readyOrders= merchantDTO.getOrderHistory();

        return ResponseEntity.ok(readyOrders);
    }


}
