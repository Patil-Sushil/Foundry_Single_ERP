package com.kalibyte.foundry.billing.invoice.controller;

import com.kalibyte.foundry.billing.invoice.dto.request.InvoiceRequest;
import com.kalibyte.foundry.billing.invoice.dto.response.InvoiceResponse;
import com.kalibyte.foundry.billing.invoice.service.InvoiceService;
import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
public class InvoiceController {

    private final InvoiceService invoiceService;

    //------------------------------------------------
    // GENERATE INVOICE
    //------------------------------------------------

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(
            @Valid @RequestBody InvoiceRequest request
    ) {

        InvoiceResponse response =
                invoiceService.generateInvoice(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    //------------------------------------------------
    // GET INVOICE
    //------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(
            @PathVariable UUID id) {

        InvoiceResponse response =
                invoiceService.getInvoice(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    //------------------------------------------------
    // DOWNLOAD INVOICE PDF
    //------------------------------------------------

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {

        byte[] pdf = invoiceService.generateInvoicePdf(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=invoice-" + id + ".pdf")
                .body(pdf);
    }

    //------------------------------------------------
    // Get All Invoice
    //------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                ApiResponse.success(
                        invoiceService.getAllInvoices(pageable)
                )
        );
    }
}