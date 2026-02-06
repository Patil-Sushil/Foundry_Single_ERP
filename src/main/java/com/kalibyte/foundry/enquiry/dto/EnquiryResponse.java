package com.kalibyte.foundry.enquiry.dto;

import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.enquiry.entity.MetalCategory;
import com.kalibyte.foundry.enquiry.entity.MetalType;
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
    private String customerName;
    private BigDecimal totalWeightKg;
    private String status;

    private List<EnquiryItemResponse> items;
}
