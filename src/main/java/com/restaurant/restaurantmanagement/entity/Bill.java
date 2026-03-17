package com.restaurant.restaurantmanagement.entity;

import com.restaurant.restaurantmanagement.enums.BillStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "bill")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @OneToMany(mappedBy = "bill", fetch = FetchType.LAZY)
    private List<Order> orders;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal taxRate;

    @Column(nullable = false)
    private BigDecimal taxAmount;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillStatus status = BillStatus.UNPAID;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
