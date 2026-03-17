package com.kalibyte.foundry.order.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.order.dto.request.OrderCreateRequest;
import com.kalibyte.foundry.order.dto.response.OrderResponse;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import com.kalibyte.foundry.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Create Order
    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ApiResponse.success("Order created successfully", response);
    }

    // Get Order by ID
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getById(@PathVariable UUID id) {
        OrderResponse response = orderService.getById(id);
        return ApiResponse.success(response);
    }

    // Get Orders with filters
    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> getAll(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable) {

        PageResponse<OrderResponse> response =
                orderService.getAll(status, customerId, from, to, pageable);

        return ApiResponse.success(response);
    }

    // Update Order Status
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status) throws BadRequestException {

        orderService.updateStatus(id, status);
        return ApiResponse.success("Order status updated successfully", null);
    }
}