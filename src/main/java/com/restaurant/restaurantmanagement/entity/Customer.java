package com.restaurant.restaurantmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"table"})
@Entity
@Table(name = "customer")
@DiscriminatorValue("CUSTOMER")
public class Customer extends BaseUser {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private RestaurantTable table;

    private String sessionToken;

    private String otp;

    private LocalDateTime otpExpiry;
}