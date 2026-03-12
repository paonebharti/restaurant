package com.restaurant.restaurantmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class MenuCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private List<MenuItemResponse> items;
}
