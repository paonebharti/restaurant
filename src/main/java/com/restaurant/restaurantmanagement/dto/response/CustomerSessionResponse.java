package com.restaurant.restaurantmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerSessionResponse {
    private String sessionToken;
    private String fullName;
    private String tableNumber;
    private String message;
}
