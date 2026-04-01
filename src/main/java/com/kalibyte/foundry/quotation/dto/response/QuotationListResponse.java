package com.kalibyte.foundry.quotation.dto.response;

import com.kalibyte.foundry.quotation.entity.enums.QuotationStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class QuotationListResponse {
    private UUID id;
    private String quotationNumber;
    private String enquiryNumber;
    private String customerName;
    private LocalDate quotationDate;
    private QuotationStatus status;
    private BigDecimal totalAmount;

    public void setId(UUID id) {
        this.id = id != null ? UUID.nameUUIDFromBytes(id.toString().getBytes()) : null;
    }
}
