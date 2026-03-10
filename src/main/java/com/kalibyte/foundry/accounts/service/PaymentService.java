package com.kalibyte.foundry.accounts.service;

import com.kalibyte.foundry.accounts.dto.request.PaymentCreateRequest;
import com.kalibyte.foundry.accounts.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPayment(PaymentCreateRequest request);

    PaymentResponse getPayment(UUID id);

    List<PaymentResponse> getPaymentsByInvoice(UUID invoiceId);

    List<PaymentResponse> getAllPayments();
}
