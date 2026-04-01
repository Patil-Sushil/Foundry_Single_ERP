package com.kalibyte.foundry.enquiry.dto.response;

import com.kalibyte.foundry.enquiry.entity.enums.EnquiryStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class EnquiryResponse {
    private UUID id;
    private String enquiryNo;
    private LocalDate enquiryDate;
    private UUID customerId;
    private String customerName;
    private BigDecimal totalWeightKg;
    private EnquiryStatus status;

    private List<EnquiryItemResponse> items;
}
