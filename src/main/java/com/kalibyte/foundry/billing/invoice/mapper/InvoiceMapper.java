package com.kalibyte.foundry.billing.invoice.mapper;

import com.kalibyte.foundry.billing.invoice.dto.response.InvoiceItemResponse;
import com.kalibyte.foundry.billing.invoice.dto.response.InvoiceResponse;
import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.entity.InvoiceItem;
import org.mapstruct.*;

import java.util.List;

import com.kalibyte.foundry.inventory.purchaseinvoice.dto.response.PurchaseInvoiceResponse;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.response.PurchaseInvoiceSummary;
import com.kalibyte.foundry.inventory.purchaseinvoice.entity.PurchaseInvoice;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface InvoiceMapper {

    // =========================================================
    //  INVOICE -> INVOICE RESPONSE
    // =========================================================

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "items", source = "items")
    InvoiceResponse toResponse(Invoice invoice);

    List<InvoiceResponse> toResponseList(List<Invoice> invoices);

    // =========================================================
    //  INVOICE ITEM -> INVOICE ITEM RESPONSE
    // =========================================================

    @Mapping(target = "partName", source = "orderItem.partName")
    @Mapping(target = "materialGrade", source = "orderItem.materialGrade")
    InvoiceItemResponse toItemResponse(InvoiceItem item);

    List<InvoiceItemResponse> toItemResponseList(List<InvoiceItem> items);

    // =========================================================
    //  PURCHASE INVOICE -> PURCHASE INVOICE RESPONSE/SUMMARY
    // =========================================================

    @Mapping(target = "vendorId", source = "vendor.id")
    @Mapping(target = "vendorName", source = "vendor.name")
    @Mapping(target = "vendorGstin", source = "vendor.gstNumber")
    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "poNumber", source = "purchaseOrder.poNumber")
    @Mapping(target = "materialInwardId", source = "materialInward.id")
    @Mapping(target = "inwardNumber", source = "materialInward.inwardNumber")
    @Mapping(target = "inwardAmount", source = "materialInward.totalAmount")
    @Mapping(target = "hasAmountMismatch", expression = "java(pi.hasAmountMismatch())")
    PurchaseInvoiceResponse toPurchaseInvoiceResponse(PurchaseInvoice pi);

    @Mapping(target = "vendorName", source = "vendor.name")
    @Mapping(target = "poNumber", source = "purchaseOrder.poNumber")
    @Mapping(target = "inwardNumber", source = "materialInward.inwardNumber")
    @Mapping(target = "hasAmountMismatch", expression = "java(pi.hasAmountMismatch())")
    PurchaseInvoiceSummary toPurchaseInvoiceSummary(PurchaseInvoice pi);
}