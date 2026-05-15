package com.kalibyte.foundry.inventory.item.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.security.UserPrincipal;
import com.kalibyte.foundry.inventory.item.dto.request.CreateItemRequest;
import com.kalibyte.foundry.inventory.item.dto.request.StockAdjustmentRequest;
import com.kalibyte.foundry.inventory.item.dto.request.UpdateItemRequest;
import com.kalibyte.foundry.inventory.item.dto.response.ItemResponse;
import com.kalibyte.foundry.inventory.item.dto.response.ItemSummary;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.service.ItemService;
import com.kalibyte.foundry.inventory.item.service.StockAdjustmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/items")
@Tag(name = "Items", description = "Item Management APIs")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final StockAdjustmentService stockAdjustmentService;

	@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ItemResponse> create(
            @Valid @RequestBody CreateItemRequest request) {
        return ApiResponse.success("Item created successfully", itemService.create(request));
    }

    @Operation(summary = "Adjust item stock", description = "Manually adjust item stock levels. Positive quantities act as inwards (requiring unit rate), negative as issues.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock adjusted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid adjustment request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    })
    @PostMapping("/{itemId}/adjust-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    public ApiResponse<ItemResponse> adjustStock(
            @Parameter(description = "ID of the item to adjust") @PathVariable Long itemId,
            @Valid @RequestBody StockAdjustmentRequest request,
            UserPrincipal userPrincipal) {
        return ApiResponse.success("Stock adjusted successfully", 
                stockAdjustmentService.adjustStock(itemId, request, userPrincipal));
    }

    @GetMapping
    public ApiResponse<PageResponse<ItemResponse>> getAll(
            @RequestParam(required = false) ItemCategory category,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isScrap,
            Pageable pageable) {
        return ApiResponse.success("Items retrieved successfully", PageResponse.from(itemService.getAll(category, isActive, isScrap, pageable)));
    }

    @GetMapping("/{id}")
    public ApiResponse<ItemResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Item retrieved successfully", itemService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ItemResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        return ApiResponse.success("Item updated successfully", itemService.update(id, request));
    }

    @PatchMapping("/toggle-status/{id}")
    public ApiResponse<ItemResponse> toggleItem(@PathVariable Long id){
        return ApiResponse.success("Updated Status ",itemService.toggleStatus(id));
    }

    @GetMapping("/search")
    public ApiResponse<List<ItemSummary>> search(
            @RequestParam String q,
            @RequestParam(required = false) Boolean isScrap) {
        return ApiResponse.success("Items searched successfully", itemService.search(q, isScrap));
    }

    @GetMapping("/low-stock")
    public ApiResponse<List<ItemResponse>> getLowStockItems() {
        return ApiResponse.success("Low stock items retrieved successfully", itemService.getLowStockItems());
    }
}
