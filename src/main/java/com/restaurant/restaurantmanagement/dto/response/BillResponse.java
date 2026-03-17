package com.restaurant.restaurantmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class BillResponse {
    private Long billId;
    private String customerName;
    private String tableNumber;
    private List<BillItemResponse> orders;
    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}
