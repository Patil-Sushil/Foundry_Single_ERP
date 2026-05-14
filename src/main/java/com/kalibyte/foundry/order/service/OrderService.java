package com.kalibyte.foundry.order.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.enquiry.entity.enums.MetalType;
import com.kalibyte.foundry.order.dto.request.OrderCreateRequest;
import com.kalibyte.foundry.order.dto.response.OrderItemResponse;
import com.kalibyte.foundry.order.dto.response.OrderResponse;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface OrderService {

    /**
     * Create Order
     * Supports:
     *  - From approved quotation
     *  - Direct order for customer
     */
    OrderResponse createOrder(OrderCreateRequest request);

    // Get Order by ID (Full details including customer, quotation, items)
    OrderResponse getById(UUID id);

    // Get All Orders with filtering & pagination
    PageResponse<OrderResponse> getAll(
            OrderStatus status,
            UUID customerId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );

    // Update Order Status (Workflow validated)
    void updateStatus(UUID id, OrderStatus status);

    // Get Pending Orders
    PageResponse<OrderResponse> getPendingOrders(Pageable pageable);

    // =========================================================
    //  ORDER ITEM APIs
    // =========================================================

    /**
     * Get all order items across all orders with filters.
     */
    PageResponse<OrderItemResponse> getAllOrderItems(
            UUID orderId,
            UUID customerId,
            OrderStatus orderStatus,
            String partName,
            MetalType metalType,
            UUID castingProcessId,
            Boolean pendingOnly,
            Pageable pageable
    );

    /**
     * Get a single order item by its ID.
     */
    OrderItemResponse getOrderItemById(UUID itemId);

    /**
     * Get all pending/not-fully-produced order items.
     */
    PageResponse<OrderItemResponse> getPendingOrderItems(Pageable pageable);
}