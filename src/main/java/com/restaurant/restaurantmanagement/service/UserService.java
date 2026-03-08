package com.restaurant.restaurantmanagement.service;

import com.restaurant.restaurantmanagement.dto.request.RegisterRequest;
import com.restaurant.restaurantmanagement.dto.response.RegisterResponse;
import com.restaurant.restaurantmanagement.enums.Role;
import com.restaurant.restaurantmanagement.entity.User;
import com.restaurant.restaurantmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public RegisterResponse registerUser(RegisterRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Map DTO to Entity
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // ⚠️ plain text for now, we'll hash it with Spring Security later
        user.setPhone(request.getPhone());
        user.setRole(Role.CUSTOMER); // default role

        // Save to DB
        User savedUser = userRepository.save(user);

        // Map Entity to Response DTO and return
        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getRole().name(),
                "User registered successfully!"
        );
    }
}
