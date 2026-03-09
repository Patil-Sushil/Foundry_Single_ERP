package com.kalibyte.foundry.accounts.controller;

import com.kalibyte.foundry.accounts.dto.request.PaymentCreateRequest;
import com.kalibyte.foundry.accounts.dto.response.PaymentResponse;
import com.kalibyte.foundry.accounts.service.PaymentService;
import com.kalibyte.foundry.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(
            @RequestBody PaymentCreateRequest request){

        return ApiResponse.success(
                paymentService.createPayment(request)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPayment(
            @PathVariable UUID id){

        return ApiResponse.success(
                paymentService.getPayment(id)
        );
    }

    @GetMapping("/invoice/{invoiceId}")
    public ApiResponse<List<PaymentResponse>> getPaymentsByInvoice(
            @PathVariable UUID invoiceId){

        return ApiResponse.success(
                paymentService.getPaymentsByInvoice(invoiceId)
        );
    }

    @GetMapping
    public ApiResponse<List<PaymentResponse>> getAllPayments(){

        return ApiResponse.success(
                paymentService.getAllPayments()
        );
    }
}