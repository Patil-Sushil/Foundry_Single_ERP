package com.kalibyte.foundry.enquiry.dto;

import com.kalibyte.foundry.enquiry.entity.ENUM.CastingProcess;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class EnquiryItemCreateRequest {

    @NotBlank
    private String partName;

    @NotNull
    private Long metalCategoryId;

    @NotNull
    private Long metalTypeId;

    @NotNull
    @Positive
    private Integer requiredQuantity;

    @NotNull
    @Positive
    private BigDecimal approxPieceWeightKg;

    private Boolean patternAvailable;
    private Boolean machineRequired;

    @NotNull
    private String castingProcess;
}


