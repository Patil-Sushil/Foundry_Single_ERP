package com.kalibyte.foundry.payment.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.payment.dto.request.PaymentCancelRequest;
import com.kalibyte.foundry.payment.dto.request.PaymentCreateRequest;
import com.kalibyte.foundry.payment.dto.request.PaymentFilterRequest;
import com.kalibyte.foundry.payment.dto.response.PaymentResponse;
import com.kalibyte.foundry.payment.dto.response.PaymentSummaryResponse;
import com.kalibyte.foundry.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ── Create Payment ──
    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentCreateRequest request) {

        return ApiResponse.success(
                paymentService.createPayment(request)
        );
    }

    // ── Get Single Payment ──
    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPayment(
            @PathVariable UUID id) {

        return ApiResponse.success(
                paymentService.getPayment(id)
        );
    }

    // ── Get Payments by Invoice ──
    @GetMapping("/invoice/{invoiceId}")
    public ApiResponse<List<PaymentResponse>> getPaymentsByInvoice(
            @PathVariable UUID invoiceId) {

        return ApiResponse.success(
                paymentService.getPaymentsByInvoice(invoiceId)
        );
    }

    // ── Invoice Payment Summary ──
    @GetMapping("/invoice/{invoiceId}/summary")
    public ApiResponse<PaymentSummaryResponse> getInvoicePaymentSummary(
            @PathVariable UUID invoiceId) {

        return ApiResponse.success(
                paymentService.getInvoicePaymentSummary(invoiceId)
        );
    }

    // ── Search / Filter with Pagination ──
    @PostMapping("/search")
    public ApiResponse<Page<PaymentResponse>> searchPayments(
            @RequestBody PaymentFilterRequest filter) {

        return ApiResponse.success(
                paymentService.searchPayments(filter)
        );
    }

    // ── Cancel Payment ──
    @PatchMapping("/{id}/cancel")
    public ApiResponse<PaymentResponse> cancelPayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentCancelRequest request) {

        return ApiResponse.success(
                paymentService.cancelPayment(id, request)
        );
    }

    // ── Mark Cheque/DD as Bounced ──
    @PatchMapping("/{id}/bounce")
    public ApiResponse<PaymentResponse> markAsBounced(
            @PathVariable UUID id,
            @RequestParam String reason) {

        return ApiResponse.success(
                paymentService.markAsBounced(id, reason)
        );
    }

    // ── Confirm Cheque/DD Cleared ──
    @PatchMapping("/{id}/confirm-clearance")
    public ApiResponse<PaymentResponse> confirmChequeCleared(
            @PathVariable UUID id) {

        return ApiResponse.success(
                paymentService.confirmChequeCleared(id)
        );
    }
}