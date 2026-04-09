package com.kalibyte.foundry.reports.gst.dto.response.b2b;

import com.kalibyte.foundry.order.entity.enums.GstType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class B2BCreditNoteItem {
    private UUID creditNoteId;
    private String creditNoteNumber;
    private LocalDate issueDate;
    private String originalInvoiceNumber;
    private GstType gstType;
    private BigDecimal taxableValue;
    private BigDecimal gstRate;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalGst;
    private BigDecimal totalAmount;
}
