package com.restaurant.restaurantmanagement.controller;

import com.restaurant.restaurantmanagement.dto.request.InitiatePaymentRequest;
import com.restaurant.restaurantmanagement.dto.request.VerifyPaymentRequest;
import com.restaurant.restaurantmanagement.dto.response.PaymentResponse;
import com.restaurant.restaurantmanagement.entity.Customer;
import com.restaurant.restaurantmanagement.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @AuthenticationPrincipal Customer customer,
            @Valid @RequestBody InitiatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(customer, request));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @AuthenticationPrincipal Customer customer,
            @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(paymentService.verifyPayment(customer, request));
    }
}
