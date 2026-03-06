package com.kalibyte.foundry.billing.mapper;


import com.kalibyte.foundry.billing.dto.response.InvoiceResponse;
import com.kalibyte.foundry.billing.entity.Invoice;

public class InvoiceMapper {

    public static InvoiceResponse toResponse(Invoice invoice) {

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderId(invoice.getOrder().getId())
                .vehicleNumber(invoice.getVehicleNumber())
                .subtotal(invoice.getSubtotal())
                .cgst(invoice.getCgst())
                .sgst(invoice.getSgst())
                .igst(invoice.getIgst())
                .totalAmount(invoice.getTotalAmount())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .billStatus(invoice.getBillStatus())
                .build();
    }
}
