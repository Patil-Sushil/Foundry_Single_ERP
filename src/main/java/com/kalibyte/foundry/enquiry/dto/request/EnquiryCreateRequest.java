package com.kalibyte.foundry.enquiry.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class EnquiryCreateRequest {

    @NotNull
    private UUID customerId;

    @NotNull
    private LocalDate enquiryDate;

    @Valid
    @NotEmpty(message = "At least one enquiry item is required")
    private List<EnquiryItemCreateRequest> enquiryItems;
}