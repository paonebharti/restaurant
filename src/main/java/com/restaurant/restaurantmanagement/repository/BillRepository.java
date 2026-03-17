package com.restaurant.restaurantmanagement.repository;

import com.restaurant.restaurantmanagement.entity.Bill;
import com.restaurant.restaurantmanagement.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    @Query("SELECT b FROM Bill b " +
            "JOIN FETCH b.customer " +
            "JOIN FETCH b.table " +
            "WHERE b.id = :id")
    Optional<Bill> findByIdWithDetails(
            @org.springframework.data.repository.query.Param("id") Long id);

    @Query("SELECT b FROM Bill b " +
            "JOIN FETCH b.customer " +
            "JOIN FETCH b.table " +
            "WHERE b.customer.id = :customerId " +
            "AND b.status = :status")
    Optional<Bill> findByCustomerIdAndStatus(
            @org.springframework.data.repository.query.Param("customerId") Long customerId,
            @org.springframework.data.repository.query.Param("status") BillStatus status);

    List<Bill> findByTableId(Long tableId);
}
