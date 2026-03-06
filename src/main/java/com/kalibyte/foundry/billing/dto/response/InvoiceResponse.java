package com.kalibyte.foundry.billing.dto.response;

import com.kalibyte.foundry.billing.ENUM.BillStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private UUID id;

    private String invoiceNumber;

    private UUID orderId;

    private String vehicleNumber;

    private BigDecimal subtotal;

    private BigDecimal cgst;

    private BigDecimal sgst;

    private BigDecimal igst;

    private BigDecimal totalAmount;

    private LocalDate invoiceDate;

    private LocalDate dueDate;

    private BillStatus billStatus;

}
