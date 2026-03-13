package com.restaurant.restaurantmanagement.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class PlaceOrderRequest {

    @NotNull(message = "Items are required")
    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequest> items;
}
