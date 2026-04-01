package com.kalibyte.foundry.billing.invoice.mapper;

import com.kalibyte.foundry.billing.invoice.dto.response.InvoiceItemResponse;
import com.kalibyte.foundry.billing.invoice.dto.response.InvoiceResponse;
import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.entity.InvoiceItem;
import org.mapstruct.*;

import java.util.List;

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
}