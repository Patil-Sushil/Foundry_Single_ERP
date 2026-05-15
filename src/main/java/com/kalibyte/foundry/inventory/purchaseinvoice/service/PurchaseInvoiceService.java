package com.kalibyte.foundry.inventory.purchaseinvoice.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.request.CreatePurchaseInvoiceRequest;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.request.UpdatePurchaseInvoiceRequest;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.response.PurchaseInvoiceResponse;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.response.PurchaseInvoiceSummary;
import com.kalibyte.foundry.inventory.purchaseinvoice.entity.PurchaseInvoice;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseInvoiceService {
    PurchaseInvoiceResponse create(CreatePurchaseInvoiceRequest request);
    PurchaseInvoiceResponse linkToInward(Long invoiceId, Long inwardId);
    PurchaseInvoiceResponse update(Long id, UpdatePurchaseInvoiceRequest request);
    PurchaseInvoiceResponse verify(Long id);
    PurchaseInvoiceResponse getById(Long id);
    List<PurchaseInvoiceResponse> getByInwardId(Long inwardId);
    List<PurchaseInvoiceResponse> getByPurchaseOrderId(Long poId);
    List<PurchaseInvoiceResponse> getUnlinkedInvoices(Long vendorId);
    PageResponse<PurchaseInvoiceSummary> getAll(Long vendorId, Boolean verified, LocalDate from, LocalDate to, Boolean hasInward, Pageable pageable);
    List<PurchaseInvoiceResponse> getGstReport(LocalDate from, LocalDate to);
    List<PurchaseInvoice> getInvoicesForExport(LocalDate from, LocalDate to);
}
