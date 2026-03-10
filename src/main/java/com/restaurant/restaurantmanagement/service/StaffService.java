package com.restaurant.restaurantmanagement.service;

import com.restaurant.restaurantmanagement.dto.request.StaffLoginRequest;
import com.restaurant.restaurantmanagement.dto.request.StaffRegisterRequest;
import com.restaurant.restaurantmanagement.dto.response.AuthResponse;
import com.restaurant.restaurantmanagement.enums.Role;
import com.restaurant.restaurantmanagement.entity.Staff;
import com.restaurant.restaurantmanagement.repository.StaffRepository;
import com.restaurant.restaurantmanagement.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(StaffRegisterRequest request) {

        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Staff staff = new Staff();
        staff.setFullName(request.getFullName());
        staff.setEmail(request.getEmail());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setPhone(request.getPhone());
        staff.setRole(Role.valueOf(request.getRole().toUpperCase()));

        Staff saved = staffRepository.save(staff);

        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name());

        return new AuthResponse(
                token,
                saved.getFullName(),
                saved.getEmail(),
                saved.getRole().name(),
                "Staff registered successfully!"
        );
    }

    public AuthResponse login(StaffLoginRequest request) {

        Staff staff = staffRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), staff.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!staff.isActive()) {
            throw new RuntimeException("Account is deactivated. Contact admin.");
        }

        String token = jwtUtil.generateToken(staff.getEmail(), staff.getRole().name());

        return new AuthResponse(
                token,
                staff.getFullName(),
                staff.getEmail(),
                staff.getRole().name(),
                "Login successful!"
        );
    }
}
