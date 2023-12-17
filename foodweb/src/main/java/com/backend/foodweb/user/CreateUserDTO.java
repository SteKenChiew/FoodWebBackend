package com.backend.foodweb.user;

import com.backend.foodweb.cart.CartDTO;
import com.backend.foodweb.cart.Order;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
public class CreateUserDTO {
    private String username;
    private String email;
    private String hashedpassword;
    private String token;
    private String UUID;
    private CartDTO cart;
    private List<Order> activeOrders;
    private List<Order> orderHistory;
    public CreateUserDTO() {
        this.cart = new CartDTO();
    }
    public List<Order> getActiveOrders() {
        if (activeOrders == null) {
            activeOrders = new ArrayList<>();
        }
        return activeOrders;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public void moveOrderToHistory(String orderId) {
        System.out.println("Entering moveOrderToHistory");
        System.out.println("Order ID to move: " + orderId);

        if (orderHistory == null) {
            orderHistory = new ArrayList<>();
        }

        Optional<Order> activeOrder = Optional.ofNullable(activeOrders)
                .orElse(Collections.emptyList())
                .stream()
                .filter(order -> order.getOrderId().equals(orderId))
                .findFirst();

        activeOrder.ifPresent(order -> {
            order.setStatus("done");
            System.out.println("Moving order to history: " + order);
            orderHistory.add(order);
            activeOrders.remove(order);

        });

        System.out.println("Exiting moveOrderToHistory");
    }

    public Optional<Order> markOrderAsReady(String orderId) {
        System.out.println("Entering markOrderAsReady");
        System.out.println("Order ID to mark as ready: " + orderId);

        Optional<Order> activeOrder = Optional.ofNullable(activeOrders)
                .orElse(Collections.emptyList())
                .stream()
                .filter(order -> order.getOrderId().equals(orderId))
                .findFirst();

        activeOrder.ifPresent(order -> {
            order.setStatus("ready");  // Update the status to "ready"
            System.out.println("Marking order as ready: " + order);
        });

        System.out.println("Exiting markOrderAsReady");
        return activeOrder;
    }
}
