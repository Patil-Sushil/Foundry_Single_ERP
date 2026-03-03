package com.kalibyte.foundry.enquiry.dto.request;


import com.kalibyte.foundry.enquiry.entity.ENUM.EnquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEnquiryStatusRequest {

    @NotNull(message = "Status is required")
    private EnquiryStatus status;
}