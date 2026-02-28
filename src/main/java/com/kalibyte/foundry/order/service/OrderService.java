package com.kalibyte.foundry.order.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.order.dto.request.OrderCreateRequest;
import com.kalibyte.foundry.order.dto.response.OrderResponse;
import com.kalibyte.foundry.order.entity.OrderStatus;
import org.apache.coyote.BadRequestException;
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

    // Delete Order (Optional - if needed)
    // void delete(UUID id);
}