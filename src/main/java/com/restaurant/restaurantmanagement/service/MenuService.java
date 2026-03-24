package com.restaurant.restaurantmanagement.service;

import com.restaurant.restaurantmanagement.dto.request.MenuCategoryRequest;
import com.restaurant.restaurantmanagement.dto.request.MenuItemRequest;
import com.restaurant.restaurantmanagement.dto.response.MenuCategoryResponse;
import com.restaurant.restaurantmanagement.dto.response.MenuItemResponse;
import com.restaurant.restaurantmanagement.enums.FoodType;
import com.restaurant.restaurantmanagement.entity.MenuCategory;
import com.restaurant.restaurantmanagement.entity.MenuItem;
import com.restaurant.restaurantmanagement.exception.ConflictException;
import com.restaurant.restaurantmanagement.exception.ResourceNotFoundException;
import com.restaurant.restaurantmanagement.repository.MenuCategoryRepository;
import com.restaurant.restaurantmanagement.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;

    // ─── Category Operations ───────────────────────────────

    public MenuCategoryResponse addCategory(MenuCategoryRequest request) {
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new ConflictException("Category already exists");
        }

        MenuCategory category = new MenuCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        MenuCategory saved = categoryRepository.save(category);
        return mapCategoryToResponse(saved);
    }

    public List<MenuCategoryResponse> getAllCategories() {
        return categoryRepository.findByActiveTrue()
                .stream()
                .map(this::mapCategoryToResponse)
                .collect(Collectors.toList());
    }

    public MenuCategoryResponse updateCategory(Long id, MenuCategoryRequest request) {
        MenuCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return mapCategoryToResponse(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        MenuCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setActive(false);
        categoryRepository.save(category);
    }

    // ─── Item Operations ───────────────────────────────────

    public MenuItemResponse addItem(MenuItemRequest request) {
        MenuCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        MenuItem item = new MenuItem();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setFoodType(FoodType.valueOf(request.getFoodType().toUpperCase()));
        item.setImageUrl(request.getImageUrl());
        item.setCategory(category);

        return mapItemToResponse(itemRepository.save(item));
    }

    public List<MenuItemResponse> getFullMenu() {
        return itemRepository.findByAvailableTrue()
                .stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getMenuByCategory(Long categoryId) {
        return itemRepository.findByCategoryIdAndAvailableTrue(categoryId)
                .stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getMenuByFoodType(String foodType) {
        return itemRepository.findByFoodTypeAndAvailableTrue(
                        FoodType.valueOf(foodType.toUpperCase()))
                .stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
    }

    public MenuItemResponse updateItem(Long id, MenuItemRequest request) {
        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        MenuCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setFoodType(FoodType.valueOf(request.getFoodType().toUpperCase()));
        item.setImageUrl(request.getImageUrl());
        item.setCategory(category);

        return mapItemToResponse(itemRepository.save(item));
    }

    public void toggleItemAvailability(Long id) {
        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        item.setAvailable(!item.isAvailable());
        itemRepository.save(item);
    }

    // ─── Mappers ───────────────────────────────────────────

    private MenuCategoryResponse mapCategoryToResponse(MenuCategory category) {
        List<MenuItemResponse> items = category.getItems() == null ? List.of() :
                category.getItems().stream()
                        .filter(MenuItem::isAvailable)
                        .map(this::mapItemToResponse)
                        .collect(Collectors.toList());

        return new MenuCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                items
        );
    }

    private MenuItemResponse mapItemToResponse(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getFoodType().name(),
                item.getImageUrl(),
                item.isAvailable(),
                item.getCategory().getName()
        );
    }
}
