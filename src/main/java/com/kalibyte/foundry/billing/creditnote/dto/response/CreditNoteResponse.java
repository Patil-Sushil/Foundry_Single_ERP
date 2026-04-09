package com.kalibyte.foundry.billing.creditnote.dto.response;

import com.kalibyte.foundry.billing.creditnote.entity.enums.CreditNoteStatus;
import com.kalibyte.foundry.order.entity.enums.GstType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreditNoteResponse {
    private UUID id;
    private String creditNoteNumber;
    private UUID customerId;
    private String customerName;
    private UUID orderId;
    private String orderNumber;
    private UUID invoiceId;
    private String originalInvoiceNumber;
    private Long customerReturnId;
    private String returnNumber;
    private LocalDate issueDate;
    private String reason;
    private BigDecimal subtotal;
    private GstType gstType;
    private BigDecimal gstPercentage;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalGst;
    private BigDecimal totalAmount;
    private CreditNoteStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
}
