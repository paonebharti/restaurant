package com.restaurant.restaurantmanagement.repository;

import com.restaurant.restaurantmanagement.entity.Order;
import com.restaurant.restaurantmanagement.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.customer " +
            "JOIN FETCH o.table " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menuItem " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@org.springframework.data.repository.query.Param("id") Long id);
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.customer " +
            "JOIN FETCH o.table " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menuItem " +
            "WHERE o.status = :status")
    List<Order> findByStatusWithDetails(
            @org.springframework.data.repository.query.Param("status") OrderStatus status);
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.customer " +
            "JOIN FETCH o.table " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menuItem " +
            "WHERE o.table.id = :tableId AND o.status != :status")
    List<Order> findByTableIdAndStatusNotWithDetails(
            @org.springframework.data.repository.query.Param("tableId") Long tableId,
            @org.springframework.data.repository.query.Param("status") OrderStatus status);
    List<Order> findByCustomerId(Long customerId);
}
