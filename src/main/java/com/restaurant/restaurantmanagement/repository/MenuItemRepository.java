package com.restaurant.restaurantmanagement.repository;

import com.restaurant.restaurantmanagement.entity.MenuItem;
import com.restaurant.restaurantmanagement.enums.FoodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryIdAndAvailableTrue(Long categoryId);
    List<MenuItem> findByAvailableTrue();
    List<MenuItem> findByFoodTypeAndAvailableTrue(FoodType foodType);
}
