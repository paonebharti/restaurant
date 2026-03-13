package com.restaurant.restaurantmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private String itemName;
    private Integer quantity;
    private BigDecimal priceAtTimeOfOrder;
    private BigDecimal subtotal;
}
