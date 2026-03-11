package com.restaurant.restaurantmanagement.controller;

import com.restaurant.restaurantmanagement.dto.request.OtpRequest;
import com.restaurant.restaurantmanagement.dto.request.OtpVerifyRequest;
import com.restaurant.restaurantmanagement.dto.response.CustomerSessionResponse;
import com.restaurant.restaurantmanagement.dto.response.OtpResponse;
import com.restaurant.restaurantmanagement.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/request-otp")
    public ResponseEntity<OtpResponse> requestOtp(
            @Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(customerService.requestOtp(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<CustomerSessionResponse> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(customerService.verifyOtp(request));
    }
}
