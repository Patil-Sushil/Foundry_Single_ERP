package com.kalibyte.foundry.order.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.order.dto.request.OrderCreateRequest;
import com.kalibyte.foundry.order.dto.response.OrderResponse;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import com.kalibyte.foundry.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order Management APIs")
public class OrderController {

    private final OrderService orderService;

    //-----------------------------------------------------
    // CREATE ORDER
    //-----------------------------------------------------
    @PostMapping
    @Operation(summary = "Create order",
            description = "Create from quotation (pass quotationId) or direct (pass customerId + items)")
    public ApiResponse<OrderResponse> create(
            @Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ApiResponse.success("Order created successfully", response);
    }

    //-----------------------------------------------------
    // GET BY ID
    //-----------------------------------------------------
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ApiResponse<OrderResponse> getById(@PathVariable UUID id) {
        OrderResponse response = orderService.getById(id);
        return ApiResponse.success(response);
    }

    //-----------------------------------------------------
    // GET ALL WITH FILTERS
    //-----------------------------------------------------
    @GetMapping
    @Operation(summary = "Get all orders with filters")
    public ApiResponse<PageResponse<OrderResponse>> getAll(
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) OrderStatus status,

            @Parameter(description = "Filter by customer ID")
            @RequestParam(required = false) UUID customerId,

            @Parameter(description = "From date (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "To date (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        PageResponse<OrderResponse> response =
                orderService.getAll(status, customerId, from, to, pageable);
        return ApiResponse.success(response);
    }

    //-----------------------------------------------------
    // GET BY CUSTOMER
    //-----------------------------------------------------
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer")
    public ApiResponse<PageResponse<OrderResponse>> getByCustomer(
            @PathVariable UUID customerId,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        PageResponse<OrderResponse> response =
                orderService.getAll(null, customerId, null, null, pageable);
        return ApiResponse.success(response);
    }

    //-----------------------------------------------------
    // GET PENDING ORDERS
    //-----------------------------------------------------
    @GetMapping("/pending")
    @Operation(summary = "Get all active/pending orders")
    public ApiResponse<PageResponse<OrderResponse>> getPendingOrders(
            @PageableDefault(size = 20, sort = "deliveryDate", direction = Sort.Direction.ASC)
            Pageable pageable) {

        PageResponse<OrderResponse> response = orderService.getPendingOrders(pageable);
        return ApiResponse.success(response);
    }

    //-----------------------------------------------------
    // UPDATE STATUS
    //-----------------------------------------------------
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ApiResponse<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status) {

        orderService.updateStatus(id, status);
        return ApiResponse.success("Order status updated successfully", null);
    }
}