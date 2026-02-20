package com.kalibyte.foundry.inventory.purchaseorder.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.security.UserPrincipal;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.CreatePurchaseOrderRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.LastPurchaseRate;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderResponse;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderSummary;
import com.kalibyte.foundry.inventory.purchaseorder.entity.enums.POStatus;
import com.kalibyte.foundry.inventory.purchaseorder.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/purchase-orders")
@Tag(name = "Purchase Orders", description = "Purchase Order Management APIs")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

	public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
		this.purchaseOrderService = purchaseOrderService;
	}

	@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseOrderResponse> create(
            @Valid @RequestBody CreatePurchaseOrderRequest request,
            UserPrincipal user) {
        return ApiResponse.success("Purchase Order created successfully", 
                purchaseOrderService.create(request, user.getUserId()));
    }

    @GetMapping
    public ApiResponse<Page<PurchaseOrderSummary>> getAll(
            @RequestParam(required = false) POStatus status,
            @RequestParam(required = false) Long vendorId,
            Pageable pageable) {
        return ApiResponse.success("Purchase Orders retrieved successfully", 
                purchaseOrderService.getAll(status, vendorId, pageable));
    }

    @GetMapping("/open")
    public ApiResponse<List<PurchaseOrderSummary>> getOpenOrders() {
        return ApiResponse.success("Open Purchase Orders retrieved successfully", 
                purchaseOrderService.getOpenOrders());
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrderResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Purchase Order retrieved successfully", 
                purchaseOrderService.getById(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<PurchaseOrderResponse> cancel(@PathVariable Long id) {
        return ApiResponse.success("Purchase Order cancelled successfully", 
                purchaseOrderService.cancel(id));
    }

    @GetMapping("/rate-hint")
    public ApiResponse<LastPurchaseRate> getRateHint(
            @RequestParam Long itemId, 
            @RequestParam Long vendorId) {
        Optional<LastPurchaseRate> rate = purchaseOrderService.getLastPurchaseRate(itemId, vendorId);
        return rate.map(ApiResponse::success)
                   .orElseGet(() -> ApiResponse.success(null));
    }
}
