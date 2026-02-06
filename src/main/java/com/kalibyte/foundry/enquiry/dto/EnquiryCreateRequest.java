package com.kalibyte.foundry.enquiry.dto;

import com.kalibyte.foundry.enquiry.entity.ENUM.CastingProcess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
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

//    @NotNull
//    private Long metalCategoryId;
//
//    @NotBlank(message = "Part name is required")
//    private String partName;
//
//
//    @NotNull
//    private Long metalTypeId;
//
//    @NotNull
//    private BigDecimal approxPieceWeight;
//
//    @NotNull
//    private Integer requiredQuantity;
//
//
//    private String referenceNumber;
//    private String remarks;

    @NotEmpty(message = "At least one enquiry item is required")
    @Valid
    private List<EnquiryItemCreateRequest> enquiryItems;
}