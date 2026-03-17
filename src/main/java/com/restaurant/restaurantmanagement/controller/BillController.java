package com.restaurant.restaurantmanagement.controller;

import com.restaurant.restaurantmanagement.dto.response.BillResponse;
import com.restaurant.restaurantmanagement.entity.Customer;
import com.restaurant.restaurantmanagement.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping("/bill")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BillResponse> generateBill(
            @AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(billService.generateBill(customer));
    }

    @GetMapping("/bill")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BillResponse> getBill(
            @AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(billService.getBill(customer));
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BillResponse> checkout(
            @AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(billService.checkout(customer));
    }
}
