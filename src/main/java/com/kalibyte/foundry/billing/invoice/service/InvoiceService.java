package com.kalibyte.foundry.billing.invoice.service;

import com.kalibyte.foundry.billing.invoice.dto.request.InvoiceRequest;
import com.kalibyte.foundry.billing.invoice.dto.response.InvoiceResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InvoiceService {

    InvoiceResponse generateInvoice(InvoiceRequest request);

    InvoiceResponse getInvoice(UUID id);

    byte[] generateInvoicePdf(UUID invoiceId);

    PageResponse<InvoiceResponse> getAllInvoices(Pageable pageable);



}
