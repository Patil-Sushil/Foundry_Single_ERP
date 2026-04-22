package com.kalibyte.foundry.inventory.purchaseorder.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.CreatePurchaseOrderRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.LastPurchaseRate;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderResponse;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderSummary;
import com.kalibyte.foundry.inventory.purchaseorder.entity.enums.POStatus;
import com.kalibyte.foundry.inventory.purchaseorder.service.PurchaseExportService;
import com.kalibyte.foundry.inventory.purchaseorder.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@Tag(name = "Purchase Orders", description = "Purchase Order Management APIs")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseExportService purchaseExportService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService, PurchaseExportService purchaseExportService) {
        this.purchaseOrderService = purchaseOrderService;
        this.purchaseExportService = purchaseExportService;
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportPurchaseReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) throws IOException {

        if (startDate == null) {
            startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        }
        if (endDate == null) {
            endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        }

        byte[] data = purchaseExportService.exportPurchaseReport(startDate, endDate);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Purchase_Report_CA.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(resource);
    }

	@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseOrderResponse> create(
            @Valid @RequestBody CreatePurchaseOrderRequest request) {
        return ApiResponse.success("Purchase Order created successfully", 
                purchaseOrderService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<PurchaseOrderSummary>> getAll(
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
        return ApiResponse.success("Last purchase rate retrieved successfully", 
                purchaseOrderService.getLastPurchaseRate(itemId, vendorId));
    }
}
