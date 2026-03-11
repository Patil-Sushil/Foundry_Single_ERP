package com.kalibyte.foundry.billing.service;

import com.kalibyte.foundry.billing.dto.request.DeliveryChallanRequest;
import com.kalibyte.foundry.billing.dto.request.InvoiceRequest;
import com.kalibyte.foundry.billing.dto.response.DeliveryChallanResponse;
import com.kalibyte.foundry.billing.dto.response.InvoiceResponse;

import java.util.UUID;

public interface BillingService {

    DeliveryChallanResponse createDeliveryChallan(DeliveryChallanRequest request);

    DeliveryChallanResponse dispatchDeliveryChallan(UUID dcId);

    InvoiceResponse generateInvoice(InvoiceRequest request);

    byte[] generateDeliveryChallanPdf(UUID dcId);

    byte[] generateInvoicePdf(UUID invoiceId);

}
