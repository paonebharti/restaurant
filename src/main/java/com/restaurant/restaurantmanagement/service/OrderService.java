package com.restaurant.restaurantmanagement.service;

import com.restaurant.restaurantmanagement.dto.request.PlaceOrderRequest;
import com.restaurant.restaurantmanagement.dto.response.OrderItemResponse;
import com.restaurant.restaurantmanagement.dto.response.OrderResponse;
import com.restaurant.restaurantmanagement.entity.*;
import com.restaurant.restaurantmanagement.enums.OrderStatus;
import com.restaurant.restaurantmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request, Customer customer) {

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setTable(customer.getTable());
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        // Create order items
        List<OrderItem> orderItems = request.getItems().stream().map(itemRequest -> {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found: "
                            + itemRequest.getMenuItemId()));

            if (!menuItem.isAvailable()) {
                throw new RuntimeException(menuItem.getName() + " is currently unavailable");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtTimeOfOrder(menuItem.getPrice());

            return orderItem;
        }).collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);
        savedOrder.setOrderItems(orderItems);

        return mapToOrderResponse(savedOrder);
    }

    public List<OrderResponse> getCustomerOrders(Customer customer) {
        return orderRepository.findByCustomerId(customer.getId())
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByTable(Long tableId) {
        return orderRepository.findByTableIdAndStatusNotWithDetails(tableId, OrderStatus.DELIVERED)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(String status) {
        return orderRepository.findByStatusWithDetails(OrderStatus.valueOf(status.toUpperCase()))
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        orderRepository.save(order);

        Order updatedOrder = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return mapToOrderResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Customer customer) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("You can only cancel your own orders");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only PENDING orders can be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Re-fetch with all associations instead of using save() return value
        Order updatedOrder = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return mapToOrderResponse(updatedOrder);
    }

    // ─── Helpers ───────────────────────────────────────────

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.PREPARING;
            case PREPARING -> next == OrderStatus.READY;
            case READY -> next == OrderStatus.DELIVERED;
            default -> false;
        };

        if (!valid) {
            throw new RuntimeException("Invalid status transition: "
                    + current + " → " + next);
        }
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItem> items = order.getOrderItems() != null
                ? order.getOrderItems()
                : orderItemRepository.findByOrderId(order.getId());

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

        BigDecimal total = itemResponses.stream()
                .map(OrderItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
                order.getId(),
                order.getCustomer().getFullName(),
                order.getTable().getTableNumber(),
                order.getStatus().name(),
                itemResponses,
                total,
                order.getCreatedAt()
        );
    }
}
