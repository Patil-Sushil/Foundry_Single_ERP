package com.kalibyte.foundry.billing.invoice.service;

import com.kalibyte.foundry.billing.invoice.dto.request.InvoiceRequest;
import com.kalibyte.foundry.billing.invoice.dto.response.InvoiceResponse;

import java.util.UUID;

public interface InvoiceService {

    InvoiceResponse generateInvoice(InvoiceRequest request);

    InvoiceResponse getInvoice(UUID id);

    byte[] generateInvoicePdf(UUID invoiceId);



}
