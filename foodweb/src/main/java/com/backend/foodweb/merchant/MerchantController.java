package com.backend.foodweb.merchant;

import com.backend.foodweb.JwtUtils;
import com.backend.foodweb.cart.Order;
import com.backend.foodweb.firebase.FirebaseService;
import com.backend.foodweb.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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
    @Autowired
    private MerchantService merchantService;
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

        // Set the duration based on the number of active orders
        int numberOfActiveOrders = activeOrders.size();
        int duration;

        // Your own logic for setting the duration based on the number of active orders
        if (numberOfActiveOrders == 0) {
            duration = 0; // 0 minutes for 0 active orders
        } else if (numberOfActiveOrders <= 5) {
            duration = 30; // 60 minutes for 0 to 5 active orders
        }else if (numberOfActiveOrders <= 10) {
            duration = 60; // 120 minutes for 6 to 10 active orders
        } else {
            duration = 120; // 180 minutes for more than 10 active orders
        }

        // Set the duration in the merchantDTO
        merchantDTO.setDuration(duration);

        // Update the merchant information in Firebase
        userService.updateMerchant(merchantDTO);


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

    @GetMapping("merchant/sales/today")
    public ResponseEntity<String> getMerchantTotalSalesToday(@RequestParam String merchantUuid) {
        // Retrieve merchantDTO from the service
        CreateMerchantDTO merchantDTO = userService.getMerchantByUUID(merchantUuid);

        // Check if merchantDTO is null
        if (merchantDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Get the merchant's order history
        List<Order> orderHistory = merchantDTO.getOrderHistory();

        // Check if orderHistory is null
        if (orderHistory == null) {
            // If orderHistory is null, there are no orders, so total sales is 0
            return ResponseEntity.ok("0.00");
        }

        // Calculate total sales for today
        double totalSalesToday = orderHistory.stream()
                .filter(order -> isToday(order.getOrderPlacedDateTime())) // Use the orderPlacedDateTime
                .mapToDouble(Order::getOrderTotal)
                .sum();

        // Format totalSalesToday with two decimal places
        String formattedTotalSales = String.format("%.2f", totalSalesToday);

        // Return the formatted total sales as a string
        return ResponseEntity.ok(formattedTotalSales);
    }


    private boolean isToday(String orderPlacedDateTime) {
        if (orderPlacedDateTime == null) {
            // Handle the case when orderPlacedDateTime is null
            return false;
        }

        try {
            LocalDateTime orderDateTime = LocalDateTime.parse(orderPlacedDateTime);
            return LocalDate.now().isEqual(orderDateTime.toLocalDate());
        } catch (DateTimeParseException e) {
            // Handle the case when parsing fails (invalid date format)
            return false;
        }
    }

    @PutMapping("merchant/update-food-availability")
    public ResponseEntity<String> updateFoodItemAvailability(
            @RequestParam String merchantEmail,
            @RequestParam int itemID,
            @RequestParam boolean itemAvailability) {
        // Implement your logic to update food availability
        merchantService.updateItemAvailability(merchantEmail, itemID, itemAvailability);
        return ResponseEntity.ok("Food availability updated successfully");
    }

}
