package com.restaurant.restaurantmanagement.repository;

import com.restaurant.restaurantmanagement.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBillId(Long billId);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}
