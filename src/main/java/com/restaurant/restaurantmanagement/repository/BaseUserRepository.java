package com.restaurant.restaurantmanagement.repository;

import com.restaurant.restaurantmanagement.entity.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseUserRepository extends JpaRepository<BaseUser, Long> {
    boolean existsByPhone(String phone);
}
