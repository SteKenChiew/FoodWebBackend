package com.backend.foodweb.cart;
import java.security.SecureRandom;
import com.backend.foodweb.firebase.DataBaseReference;
import com.backend.foodweb.firebase.FirebaseService;
import com.backend.foodweb.merchant.CreateMerchantDTO;
import com.backend.foodweb.merchant.FoodItemDTO;
import com.backend.foodweb.user.CreateUserDTO;
import com.backend.foodweb.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.*;
import java.util.*;

@RestController
public class CartController {
    @Autowired
    private FirebaseService firebaseService;
    @Autowired
    private CartService cartService;
    @Autowired
    private UserService userService;


    @PostMapping("user/cart/add")
    public ResponseEntity<Void> addToCart(
            @RequestParam String uuid,
            @RequestParam String merchantUuid,
            @RequestBody Map<String, Object> requestMap
    ) {
        CreateUserDTO userDTO = userService.getUserById(uuid);
        System.out.println("Called ");
        System.out.println("Received cartItem: " + requestMap);
        System.out.println(merchantUuid);

        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Extract the necessary fields from the requestMap
        String itemCategory = (String) requestMap.get("itemCategory");
        String itemDescription = (String) requestMap.get("itemDescription");
        String itemImg = (String) requestMap.get("itemImg");
        String itemName = (String) requestMap.get("itemName");
        double itemPrice = ((Number) requestMap.get("itemPrice")).doubleValue();
        int itemTotalSale = ((Number) requestMap.get("itemTotalSale")).intValue();
        int quantity = ((Number) requestMap.get("quantity")).intValue();
        int itemID = ((Number) requestMap.get("itemID")).intValue();

        // Initialize the cart if it is null
        if (userDTO.getCart() == null) {
            userDTO.setCart(new CartDTO());
            userDTO.getCart().setMerchantUUID(merchantUuid);
        } else {
            // Check if the cart belongs to the same merchant
            if (userDTO.getCart().getMerchantUUID() == null || !userDTO.getCart().getMerchantUUID().equals(merchantUuid)) {
                // If not, clear the cart before adding items for a new merchant
                cartService.clearCart(userDTO.getCart());
                userDTO.getCart().setMerchantUUID(merchantUuid);
            }
        }

        // Initialize the cart items list if it is null
        if (userDTO.getCart().getCartItems() == null) {
            userDTO.getCart().setCartItems(new ArrayList<>());
        }

        // Check if the item already exists in the cart
        List<CartItemDTO> cartItems = userDTO.getCart().getCartItems();
        boolean itemExists = false;

        for (CartItemDTO cartItem : cartItems) {
            if (itemID == cartItem.getFoodItem().getItemID()) {
                // Item already exists, update the quantity
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                itemExists = true;
                break;
            }
        }

        // If the item doesn't exist, add a new cart item
        if (!itemExists) {
            CartItemDTO cartItem = new CartItemDTO();
            FoodItemDTO foodItem = new FoodItemDTO();
            foodItem.setItemID(itemID);
            foodItem.setItemCategory(itemCategory);
            foodItem.setItemDescription(itemDescription);
            foodItem.setItemImg(itemImg);
            foodItem.setItemName(itemName);
            foodItem.setItemPrice(itemPrice);
            foodItem.setItemTotalSale(itemTotalSale);

            cartItem.setFoodItem(foodItem);
            cartItem.setQuantity(quantity);

            userDTO.getCart().getCartItems().add(cartItem);
        }

        System.out.println("Final userDTO: " + userDTO);

        // Save the updated userDTO back to the database
        firebaseService.writeToFirebase(DataBaseReference.USER, userDTO);

        return ResponseEntity.ok().build();
    }





    private String extractUserIdFromRequest(HttpServletRequest request) {
        // Logic to extract the user ID from request headers or wherever it is stored
        // Example: request.getHeader("userId")
        return "exampleUserId";
    }



    @GetMapping("user/cart")
    public ResponseEntity<CartDTO> getCart(@RequestParam String uuid) {
        CreateUserDTO userDTO = userService.getUserById(uuid);
        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        CartDTO cartDTO = userDTO.getCart();
        if (cartDTO == null) {
            return ResponseEntity.ok(new CartDTO()); // Return an empty cart if it's null
        }

        return ResponseEntity.ok(cartDTO);
    }

    @PutMapping("user/cart/update")
    public ResponseEntity<Void> updateCart(
            @RequestParam String uuid,
            @RequestBody Map<String, Object> requestMap
    ) {
        CreateUserDTO userDTO = userService.getUserById(uuid);
        System.out.println("Called ");
        System.out.println("Received cart update: " + requestMap);

        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Extract the necessary fields from the requestMap
        int itemID = ((Number) requestMap.get("itemID")).intValue();
        int quantity = ((Number) requestMap.get("quantity")).intValue();

        // Initialize the cart if it is null
        if (userDTO.getCart() == null) {
            userDTO.setCart(new CartDTO());
        }

        // Initialize the cart items list if it is null
        if (userDTO.getCart().getCartItems() == null) {
            userDTO.getCart().setCartItems(new ArrayList<>());
        }

        // Check if the item already exists in the cart
        List<CartItemDTO> cartItems = userDTO.getCart().getCartItems();
        boolean itemExists = false;

        for (CartItemDTO cartItem : cartItems) {
            if (itemID == cartItem.getFoodItem().getItemID()) {
                if (quantity > 0) {
                    // Update the quantity if quantity is positive
                    cartItem.setQuantity(quantity);
                } else {
                    // Remove the item if quantity is non-positive
                    cartItems.remove(cartItem);
                }

                itemExists = true;
                break;
            }
        }

        // If the item doesn't exist, you can choose to handle it as needed

        System.out.println("Final userDTO after update: " + userDTO);

        // Save the updated userDTO back to the database
        firebaseService.writeToFirebase(DataBaseReference.USER, userDTO);

        return ResponseEntity.ok().build();
    }
    @PostMapping("user/cart/place-order")
    public ResponseEntity<Map<String, String>> placeOrder(@RequestParam String uuid) {
        CreateUserDTO userDTO = userService.getUserById(uuid);
        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Get the user's cart items
        List<CartItemDTO> cartItems = userDTO.getCart().getCartItems();

        // Check if cartItems is null or empty
        if (cartItems == null || cartItems.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // No items in the cart
        }

        // Calculate the total order price
        double orderTotal = calculateOrderTotal(cartItems);

        // Create an order from the cart items
        Order order = cartService.createOrder(cartItems);
        order.setOrderTotal(orderTotal);

        // Generate a unique order number
        String orderNumber = OrderNumberGenerator.generateOrderNumber();
        order.setOrderId(orderNumber);
        String bookingId = BookingIdGenerator.generateBookingId();
        order.setBookingId(bookingId);

        // Get the merchant name using the merchantUUID
        String merchantUuid = userDTO.getCart().getMerchantUUID();
        CreateMerchantDTO merchantDTO = userService.getMerchantByUUID(merchantUuid);
        if (merchantDTO != null) {
            order.setMerchantName(merchantDTO.getMerchantName());

            // Update itemTotalSale for each food item in the merchant's inventory
            for (CartItemDTO cartItem : cartItems) {
                FoodItemDTO foodItem = cartItem.getFoodItem();
                int quantity = cartItem.getQuantity();

                // Find the corresponding food item in the merchant's inventory
                FoodItemDTO merchantFoodItem = merchantDTO.getFoodItems().stream()
                        .filter(merchantItem -> merchantItem.getItemID() == foodItem.getItemID())
                        .findFirst()
                        .orElse(null);

                if (merchantFoodItem != null) {
                    // Update itemTotalSale for the merchant food item
                    merchantFoodItem.setItemTotalSale(merchantFoodItem.getItemTotalSale() + quantity);
                }
            }

            // Save the updated merchantDTO back to the database
            firebaseService.writeToFirebaseMerchant(DataBaseReference.MERCHANT, merchantDTO);
        } else {
            // Handle the case where merchantDTO is null
            // This may occur if the merchant is not found by UUID
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Set the order placement date and time
        order.setOrderPlacedDateTime(LocalDateTime.now().toString());

        // Add the order to the user's active orders
        userDTO.getActiveOrders().add(order);
        addOrderToMerchant(order, merchantUuid);

        // Clear the user's cart
        userDTO.getCart().setCartItems(new ArrayList<>());
        userDTO.getCart().setMerchantUUID(null);

        // Save the updated userDTO back to the database
        firebaseService.writeToFirebase(DataBaseReference.USER, userDTO);

        // Construct the response with bookingId, orderTotal, merchantName, and order placement date and time
        Map<String, String> response = new HashMap<>();
        response.put("bookingId", bookingId);
        response.put("orderTotal", String.valueOf(orderTotal));
        response.put("merchantName", (merchantDTO != null) ? merchantDTO.getMerchantName() : "Unknown Merchant");
        response.put("orderPlacedDateTime", order.getOrderPlacedDateTime().toString());

        return ResponseEntity.ok(response);
    }



    private double calculateOrderTotal(List<CartItemDTO> cartItems) {
        double total = 0.0;
        for (CartItemDTO cartItem : cartItems) {
            total += cartItem.getQuantity() * cartItem.getFoodItem().getItemPrice();
        }
        return total;
    }

    private void addOrderToMerchant(Order order, String merchantUuid) {
        // Retrieve merchantDTO from the service
        CreateMerchantDTO merchantDTO = userService.getMerchantByUUID(merchantUuid);

        // Check if merchantDTO is null
        if (merchantDTO == null) {
            // Handle the case when merchantDTO is null
            // Log an error or throw an exception, depending on your requirements
            System.err.println("MerchantDTO is null. Unable to add order to merchant.");
            return;
        }

        // Add the order to the merchant's active orders
        merchantDTO.getActiveOrders().add(order);

        // Update the merchant's information in the database
        userService.updateMerchant(merchantDTO);
    }




    public class OrderNumberGenerator {

        private static final String PREFIX = "NN-";
        private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        private static final int RANDOM_PART_LENGTH = 4;

        public static String generateOrderNumber() {
            StringBuilder orderNumber = new StringBuilder(PREFIX);
            SecureRandom random = new SecureRandom();

            for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
                int randomIndex = random.nextInt(CHARACTERS.length());
                orderNumber.append(CHARACTERS.charAt(randomIndex));
            }

            return orderNumber.toString();
        }


    }


    public class BookingIdGenerator {

        private static final int BOOKING_ID_LENGTH = 16;
        private static final String DIGITS = "0123456789";

        public static String generateBookingId() {
            StringBuilder bookingId = new StringBuilder();
            SecureRandom random = new SecureRandom();

            for (int i = 0; i < BOOKING_ID_LENGTH; i++) {
                int randomIndex = random.nextInt(DIGITS.length());
                bookingId.append(DIGITS.charAt(randomIndex));
            }

            return bookingId.toString();
        }


    }

}