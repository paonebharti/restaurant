package com.restaurant.restaurantmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InitiatePaymentRequest {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}
