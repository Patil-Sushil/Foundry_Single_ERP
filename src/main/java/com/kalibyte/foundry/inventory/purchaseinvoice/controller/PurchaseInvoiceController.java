package com.kalibyte.foundry.inventory.purchaseinvoice.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.request.CreatePurchaseInvoiceRequest;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.request.UpdatePurchaseInvoiceRequest;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.response.PurchaseInvoiceResponse;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.response.PurchaseInvoiceSummary;
import com.kalibyte.foundry.inventory.purchaseinvoice.service.PurchaseInvoiceExportService;
import com.kalibyte.foundry.inventory.purchaseinvoice.service.PurchaseInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.List;

@RestController
@RequestMapping("/api/purchase-invoices")
@Tag(name = "Purchase Invoice", description = "Vendor invoice tracking & GST reports")
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'STORE', 'CA')")
public class PurchaseInvoiceController {

    private final PurchaseInvoiceService purchaseInvoiceService;
    private final PurchaseInvoiceExportService purchaseInvoiceExportService;

    public PurchaseInvoiceController(PurchaseInvoiceService purchaseInvoiceService,
                                     PurchaseInvoiceExportService purchaseInvoiceExportService) {
        this.purchaseInvoiceService = purchaseInvoiceService;
        this.purchaseInvoiceExportService = purchaseInvoiceExportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Manually record a vendor invoice")
    public ApiResponse<PurchaseInvoiceResponse> create(
            @Valid @RequestBody CreatePurchaseInvoiceRequest request) {
        return ApiResponse.success("Purchase invoice recorded", purchaseInvoiceService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update purchase invoice")
    public ApiResponse<PurchaseInvoiceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePurchaseInvoiceRequest request) {
        return ApiResponse.success("Purchase invoice updated", purchaseInvoiceService.update(id, request));
    }

    @PatchMapping("/{id}/link-inward/{inwardId}")
    @Operation(summary = "Link invoice to an inward")
    public ApiResponse<PurchaseInvoiceResponse> linkToInward(
            @PathVariable Long id, @PathVariable Long inwardId) {
        return ApiResponse.success("Invoice linked to inward", purchaseInvoiceService.linkToInward(id, inwardId));
    }

    @PatchMapping("/{id}/verify")
    @Operation(summary = "Mark invoice as verified")
    public ApiResponse<PurchaseInvoiceResponse> verify(@PathVariable Long id) {
        return ApiResponse.success("Invoice verified", purchaseInvoiceService.verify(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseInvoiceResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Invoice retrieved", purchaseInvoiceService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<PurchaseInvoiceSummary>> getAll(
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) Boolean hasInward,
            Pageable pageable) {
        return ApiResponse.success("Invoices retrieved",
                purchaseInvoiceService.getAll(vendorId, verified, from, to, hasInward, pageable));
    }

    @GetMapping("/by-inward/{inwardId}")
    public ApiResponse<List<PurchaseInvoiceResponse>> getByInward(@PathVariable Long inwardId) {
        return ApiResponse.success("Invoices for inward", purchaseInvoiceService.getByInwardId(inwardId));
    }

    @GetMapping("/by-po/{poId}")
    public ApiResponse<List<PurchaseInvoiceResponse>> getByPO(@PathVariable Long poId) {
        return ApiResponse.success("Invoices for PO", purchaseInvoiceService.getByPurchaseOrderId(poId));
    }

    @GetMapping("/unlinked")
    @Operation(summary = "Invoices not linked to any inward")
    public ApiResponse<List<PurchaseInvoiceResponse>> getUnlinked(@RequestParam Long vendorId) {
        return ApiResponse.success("Unlinked invoices", purchaseInvoiceService.getUnlinkedInvoices(vendorId));
    }

    @GetMapping("/gst-report")
    @Operation(summary = "GST report for CA")
    public ApiResponse<List<PurchaseInvoiceResponse>> gstReport(
            @RequestParam LocalDate from, @RequestParam LocalDate to) {
        return ApiResponse.success("GST report generated", purchaseInvoiceService.getGstReport(from, to));
    }

    @GetMapping("/export/gst-report")
    @Operation(summary = "Export GST report as Excel for CA")
    public ResponseEntity<Resource> exportGstReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) throws IOException {

        if (startDate == null) {
            startDate = LocalDate.now().with(java.time.temporal.TemporalAdjusters.firstDayOfMonth());
        }
        if (endDate == null) {
            endDate = LocalDate.now().with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
        }

        byte[] data = purchaseInvoiceExportService.exportGstReport(startDate, endDate);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Purchase_Invoice_GST_Report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(resource);
    }
}
