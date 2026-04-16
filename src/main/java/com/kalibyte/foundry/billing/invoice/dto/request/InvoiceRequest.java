package com.kalibyte.foundry.billing.invoice.dto.request;

import com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {

    private UUID orderId;

    private String vehicleNumber;

    private LocalDate invoiceDate;

    private LocalDate dueDate;

    private InvoiceStatus billStatus;

    private BigDecimal amountPaid;
}
