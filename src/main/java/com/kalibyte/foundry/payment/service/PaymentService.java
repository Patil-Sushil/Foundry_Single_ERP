package com.kalibyte.foundry.payment.service;

import com.kalibyte.foundry.payment.dto.request.PaymentCancelRequest;
import com.kalibyte.foundry.payment.dto.request.PaymentCreateRequest;
import com.kalibyte.foundry.payment.dto.request.PaymentFilterRequest;
import com.kalibyte.foundry.payment.dto.response.PaymentResponse;
import com.kalibyte.foundry.payment.dto.response.PaymentSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPayment(PaymentCreateRequest request);

    PaymentResponse getPayment(UUID id);

    List<PaymentResponse> getPaymentsByInvoice(UUID invoiceId);

    Page<PaymentResponse> searchPayments(PaymentFilterRequest filter);

    PaymentSummaryResponse getInvoicePaymentSummary(UUID invoiceId);

    PaymentResponse cancelPayment(UUID paymentId, PaymentCancelRequest request);

    PaymentResponse markAsBounced(UUID paymentId, String reason);

    PaymentResponse confirmChequeCleared(UUID paymentId);  // NEW!
}