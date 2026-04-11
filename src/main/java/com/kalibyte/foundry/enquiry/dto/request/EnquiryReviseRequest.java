package com.kalibyte.foundry.enquiry.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EnquiryReviseRequest {

    @Valid
    @NotEmpty(message = "At least one enquiry item is required")
    private List<EnquiryItemCreateRequest> items;

    private String revisionNote;   // optional comment for audit
}
