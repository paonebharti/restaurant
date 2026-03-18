package com.restaurant.restaurantmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private String paymentMethod;
    private String status;
    private BigDecimal amount;
    private String razorpayOrderId;
    private String message;
    private LocalDateTime createdAt;
}
