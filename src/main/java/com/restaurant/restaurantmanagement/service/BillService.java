package com.restaurant.restaurantmanagement.service;

import com.restaurant.restaurantmanagement.dto.response.BillItemResponse;
import com.restaurant.restaurantmanagement.dto.response.BillResponse;
import com.restaurant.restaurantmanagement.dto.response.OrderItemResponse;
import com.restaurant.restaurantmanagement.entity.*;
import com.restaurant.restaurantmanagement.enums.BillStatus;
import com.restaurant.restaurantmanagement.enums.OrderStatus;
import com.restaurant.restaurantmanagement.enums.TableStatus;
import com.restaurant.restaurantmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantTableRepository tableRepository;

    @Value("${restaurant.tax.rate}")
    private BigDecimal taxRate;

    // ─── Generate Bill ─────────────────────────────────────

    @Transactional
    public BillResponse generateBill(Customer customer) {

        // Check if bill already exists for this customer
        billRepository.findByCustomerIdAndStatus(customer.getId(), BillStatus.UNPAID)
                .ifPresent(existing -> {
                    throw new RuntimeException("Bill already generated. Bill ID: "
                            + existing.getId());
                });

        // Fetch all delivered + pending + confirmed + preparing + ready orders
        List<Order> orders = orderRepository
                .findByTableIdAndStatusNot(customer.getTable().getId(),
                        OrderStatus.CANCELLED);

        if (orders.isEmpty()) {
            throw new RuntimeException("No orders found for this table");
        }

        // Calculate subtotal
        BigDecimal subtotal = orders.stream()
                .flatMap(order -> orderItemRepository
                        .findByOrderId(order.getId()).stream())
                .map(item -> item.getPriceAtTimeOfOrder()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate tax
        BigDecimal taxAmount = subtotal
                .multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal totalAmount = subtotal.add(taxAmount);

        // Create bill
        Bill bill = new Bill();
        bill.setCustomer(customer);
        bill.setTable(customer.getTable());
        bill.setSubtotal(subtotal);
        bill.setTaxRate(taxRate);
        bill.setTaxAmount(taxAmount);
        bill.setTotalAmount(totalAmount);
        bill.setStatus(BillStatus.UNPAID);

        Bill savedBill = billRepository.save(bill);

        // Link orders to bill
        orders.forEach(order -> {
            order.setBill(savedBill);
            orderRepository.save(order);
        });

        savedBill.setOrders(orders);

        log.info("Bill generated for customer: {}, total: {}",
                customer.getFullName(), totalAmount);

        return mapToBillResponse(savedBill);
    }

    // ─── Get Bill ──────────────────────────────────────────

    public BillResponse getBill(Customer customer) {
        Bill bill = billRepository
                .findByCustomerIdAndStatus(customer.getId(), BillStatus.UNPAID)
                .orElseThrow(() -> new RuntimeException("No active bill found"));

        List<Order> orders = orderRepository.findByBillId(bill.getId());
        bill.setOrders(orders);

        return mapToBillResponse(bill);
    }

    // ─── Checkout ──────────────────────────────────────────

    @Transactional
    public BillResponse checkout(Customer customer) {

        Bill bill = billRepository
                .findByCustomerIdAndStatus(customer.getId(), BillStatus.UNPAID)
                .orElseThrow(() -> new RuntimeException(
                        "No active bill found. Please generate bill first."));

        // Mark bill as paid
        bill.setStatus(BillStatus.PAID);
        bill.setPaidAt(LocalDateTime.now());
        billRepository.save(bill);

        // Mark all orders as delivered
        List<Order> orders = orderRepository.findByBillId(bill.getId());
        orders.forEach(order -> {
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        });

        // Clear customer session
        customer.setSessionToken(null);
        customer.setTable(null);
        customerRepository.save(customer);

        // Free the table
        RestaurantTable table = bill.getTable();
        table.setStatus(TableStatus.AVAILABLE);
        tableRepository.save(table);

        bill.setOrders(orders);

        log.info("Checkout completed for customer: {}", customer.getFullName());

        return mapToBillResponse(bill);
    }

    // ─── Mapper ────────────────────────────────────────────

    private BillResponse mapToBillResponse(Bill bill) {
        List<BillItemResponse> orderResponses = bill.getOrders().stream().map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

            List<OrderItemResponse> itemResponses = items.stream().map(item ->
                    new OrderItemResponse(
                            item.getId(),
                            item.getMenuItem().getName(),
                            item.getQuantity(),
                            item.getPriceAtTimeOfOrder(),
                            item.getPriceAtTimeOfOrder()
                                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    )
            ).collect(Collectors.toList());

            BigDecimal orderTotal = itemResponses.stream()
                    .map(OrderItemResponse::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new BillItemResponse(order.getId(), itemResponses, orderTotal);
        }).collect(Collectors.toList());

        return new BillResponse(
                bill.getId(),
                bill.getCustomer().getFullName(),
                bill.getTable().getTableNumber(),
                orderResponses,
                bill.getSubtotal(),
                bill.getTaxRate(),
                bill.getTaxAmount(),
                bill.getTotalAmount(),
                bill.getStatus().name(),
                bill.getCreatedAt()
        );
    }
}
