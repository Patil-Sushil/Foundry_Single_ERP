package com.kalibyte.foundry.billing.creditnote.mapper;

import com.kalibyte.foundry.billing.creditnote.dto.response.CreditNoteResponse;
import com.kalibyte.foundry.billing.creditnote.entity.CreditNote;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface CreditNoteMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "invoiceId", source = "invoiceId")
    @Mapping(target = "originalInvoiceNumber", source = "originalInvoiceNumber")
    @Mapping(target = "customerReturnId", source = "customerReturn.id")
    @Mapping(target = "returnNumber", source = "customerReturn.returnNumber")
    CreditNoteResponse toResponse(CreditNote entity);

    List<CreditNoteResponse> toResponseList(List<CreditNote> list);
}
