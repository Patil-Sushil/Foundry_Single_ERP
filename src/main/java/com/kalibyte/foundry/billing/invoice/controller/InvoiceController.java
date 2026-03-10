package com.kalibyte.foundry.billing.invoice.controller;

import com.kalibyte.foundry.billing.invoice.dto.request.InvoiceRequest;
import com.kalibyte.foundry.billing.invoice.dto.response.InvoiceResponse;
import com.kalibyte.foundry.billing.invoice.service.InvoiceService;
import com.kalibyte.foundry.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
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
}