package com.kalibyte.foundry.billing.service.impl;

import com.kalibyte.foundry.billing.dto.request.DeliveryChallanRequest;
import com.kalibyte.foundry.billing.dto.request.InvoiceRequest;
import com.kalibyte.foundry.billing.dto.response.DeliveryChallanResponse;
import com.kalibyte.foundry.billing.dto.response.InvoiceResponse;
import com.kalibyte.foundry.billing.service.BillingService;
import com.kalibyte.foundry.billing.service.DeliveryChallanService;
import com.kalibyte.foundry.billing.service.InvoiceService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final DeliveryChallanService deliveryChallanService;
    private final InvoiceService invoiceService;

    //------------------------------------------------
    // CREATE DELIVERY CHALLAN
    //------------------------------------------------

    @Override
    public DeliveryChallanResponse createDeliveryChallan(DeliveryChallanRequest request) {
        return deliveryChallanService.createDeliveryChallan(request);
    }

    //------------------------------------------------
    // DISPATCH DELIVERY CHALLAN
    //------------------------------------------------

    @Override
    public DeliveryChallanResponse dispatchDeliveryChallan(UUID dcId) {
        return deliveryChallanService.dispatchDeliveryChallan(dcId);
    }

    //------------------------------------------------
    // GENERATE INVOICE
    //------------------------------------------------

    @Override
    public InvoiceResponse generateInvoice(InvoiceRequest request) {
        return invoiceService.generateInvoice(request);
    }

    //------------------------------------------------
    // GENERATE DELIVERY CHALLAN PDF
    //------------------------------------------------

    @Override
    public byte[] generateDeliveryChallanPdf(UUID dcId) {
        return deliveryChallanService.generateDeliveryChallanPdf(dcId);
    }

    //------------------------------------------------
    // GENERATE INVOICE PDF
    //------------------------------------------------

    @Override
    public byte[] generateInvoicePdf(UUID invoiceId) {
        return invoiceService.generateInvoicePdf(invoiceId);
    }
}