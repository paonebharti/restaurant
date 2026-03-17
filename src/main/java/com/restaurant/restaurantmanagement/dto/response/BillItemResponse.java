package com.restaurant.restaurantmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class BillItemResponse {
    private Long orderId;
    private List<OrderItemResponse> items;
    private BigDecimal orderTotal;
}
