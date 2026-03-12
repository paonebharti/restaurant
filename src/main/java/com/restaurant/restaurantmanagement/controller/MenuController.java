package com.restaurant.restaurantmanagement.controller;

import com.restaurant.restaurantmanagement.dto.request.MenuCategoryRequest;
import com.restaurant.restaurantmanagement.dto.request.MenuItemRequest;
import com.restaurant.restaurantmanagement.dto.response.MenuCategoryResponse;
import com.restaurant.restaurantmanagement.dto.response.MenuItemResponse;
import com.restaurant.restaurantmanagement.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // ─── Category Endpoints ────────────────────────────────

    @PostMapping("/category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuCategoryResponse> addCategory(
            @Valid @RequestBody MenuCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.addCategory(request));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<MenuCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(menuService.getAllCategories());
    }

    @PutMapping("/category/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody MenuCategoryRequest request) {
        return ResponseEntity.ok(menuService.updateCategory(id, request));
    }

    @DeleteMapping("/category/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        menuService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully");
    }

    // ─── Item Endpoints ────────────────────────────────────

    @PostMapping("/item")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> addItem(
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.addItem(request));
    }

    @GetMapping("/items")
    public ResponseEntity<List<MenuItemResponse>> getFullMenu() {
        return ResponseEntity.ok(menuService.getFullMenu());
    }

    @GetMapping("/items/category/{categoryId}")
    public ResponseEntity<List<MenuItemResponse>> getMenuByCategory(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(menuService.getMenuByCategory(categoryId));
    }

    @GetMapping("/items/type/{foodType}")
    public ResponseEntity<List<MenuItemResponse>> getMenuByFoodType(
            @PathVariable String foodType) {
        return ResponseEntity.ok(menuService.getMenuByFoodType(foodType));
    }

    @PutMapping("/item/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuService.updateItem(id, request));
    }

    @PatchMapping("/item/{id}/toggle-availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> toggleAvailability(@PathVariable Long id) {
        menuService.toggleItemAvailability(id);
        return ResponseEntity.ok("Item availability updated");
    }
}
