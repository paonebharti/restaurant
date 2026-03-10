package com.restaurant.restaurantmanagement.controller;

import com.restaurant.restaurantmanagement.dto.request.StaffLoginRequest;
import com.restaurant.restaurantmanagement.dto.request.StaffRegisterRequest;
import com.restaurant.restaurantmanagement.dto.response.AuthResponse;
import com.restaurant.restaurantmanagement.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody StaffRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(staffService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody StaffLoginRequest request) {
        return ResponseEntity.ok(staffService.login(request));
    }
}
