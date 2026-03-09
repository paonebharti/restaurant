package com.restaurant.restaurantmanagement.entity;

import com.restaurant.restaurantmanagement.enums.TableStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "restaurant_table")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tableNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableStatus status;

    private String qrCode;
}
