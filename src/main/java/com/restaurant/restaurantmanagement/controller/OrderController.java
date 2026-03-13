package com.restaurant.restaurantmanagement.controller;

import com.restaurant.restaurantmanagement.dto.request.PlaceOrderRequest;
import com.restaurant.restaurantmanagement.dto.response.OrderResponse;
import com.restaurant.restaurantmanagement.entity.Customer;
import com.restaurant.restaurantmanagement.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ─── Customer Endpoints ────────────────────────────────

    @PostMapping("/customer/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            @AuthenticationPrincipal Customer customer) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(request, customer));
    }

    @GetMapping("/customer/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(orderService.getCustomerOrders(customer));
    }

    @DeleteMapping("/customer/orders/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, customer));
    }

    // ─── Staff Endpoints ───────────────────────────────────

    @GetMapping("/staff/orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER', 'CHEF')")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @RequestParam(defaultValue = "PENDING") String status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/staff/orders/table/{tableId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER', 'CHEF')")
    public ResponseEntity<List<OrderResponse>> getOrdersByTable(
            @PathVariable Long tableId) {
        return ResponseEntity.ok(orderService.getOrdersByTable(tableId));
    }

    @PatchMapping("/staff/orders/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER', 'CHEF')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }
}
