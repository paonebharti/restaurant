package com.restaurant.restaurantmanagement.service;

import com.restaurant.restaurantmanagement.dto.request.OtpRequest;
import com.restaurant.restaurantmanagement.dto.request.OtpVerifyRequest;
import com.restaurant.restaurantmanagement.dto.response.CustomerSessionResponse;
import com.restaurant.restaurantmanagement.dto.response.OtpResponse;
import com.restaurant.restaurantmanagement.entity.Customer;
import com.restaurant.restaurantmanagement.entity.RestaurantTable;
import com.restaurant.restaurantmanagement.enums.TableStatus;
import com.restaurant.restaurantmanagement.repository.CustomerRepository;
import com.restaurant.restaurantmanagement.repository.RestaurantTableRepository;
import com.restaurant.restaurantmanagement.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RestaurantTableRepository tableRepository;
    private final SmsService smsService;
    private final OtpUtil otpUtil;

    @Value("${otp.expiry.minutes}")
    private int otpExpiryMinutes;

    public OtpResponse requestOtp(OtpRequest request) {

        // Validate table exists
        RestaurantTable table = tableRepository
                .findByTableNumber(request.getTableNumber())
                .orElseThrow(() -> new RuntimeException("Invalid table"));

        // Check table is available or already occupied by this customer
        if (table.getStatus() == TableStatus.OCCUPIED) {
            // Check if same phone is already sitting at this table
            customerRepository.findByPhone(request.getPhone())
                    .ifPresent(existing -> {
                        if (!existing.getTable().getId().equals(table.getId())) {
                            throw new RuntimeException("Table is currently occupied");
                        }
                    });
        }

        // Find existing customer or create new one
        Customer customer = customerRepository
                .findByPhone(request.getPhone())
                .orElse(new Customer());

        // Generate OTP
        String otp = otpUtil.generateOtp();

        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setTable(table);
        customer.setOtp(otp);
        customer.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));

        customerRepository.save(customer);

        // Send OTP via SMS
        smsService.sendOtp(request.getPhone(), otp);

        log.info("OTP requested for phone: {}", request.getPhone());

        return new OtpResponse("OTP sent successfully to your phone number.");
    }

    public CustomerSessionResponse verifyOtp(OtpVerifyRequest request) {

        Customer customer = customerRepository
                .findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("Phone number not found"));

        // Check OTP matches
        if (!customer.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        // Check OTP not expired
        if (LocalDateTime.now().isAfter(customer.getOtpExpiry())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        // Generate session token
        String sessionToken = otpUtil.generateSessionToken();

        // Update customer
        customer.setSessionToken(sessionToken);
        customer.setOtp(null);
        customer.setOtpExpiry(null);

        // Mark table as occupied
        RestaurantTable table = customer.getTable();
        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        customerRepository.save(customer);

        log.info("OTP verified for phone: {}", request.getPhone());

        return new CustomerSessionResponse(
                sessionToken,
                customer.getFullName(),
                table.getTableNumber(),
                "Welcome! You can now view the menu and place orders."
        );
    }
}
