package com.kalibyte.foundry.enquiry.dto.request;

import com.kalibyte.foundry.enquiry.entity.enums.MetalType;
import com.kalibyte.foundry.enquiry.entity.enums.PatternProvidedBy;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Enquiry Item Create Request
 * NOTE:
 * - Only capture whether pattern is provided by customer
 * - Do NOT capture pattern details here
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnquiryItemCreateRequest {

    @NotBlank(message = "Part name is required")
    private String partName;

    @NotNull(message = "Metal type is required")
    private MetalType metalType;

    @NotBlank(message = "Material grade is required")
    private String materialGrade;

    @NotNull(message = "Casting process is required")
    private UUID castingProcessId;

    @NotNull(message = "Required quantity is mandatory")
    @Min(value = 1, message = "Quantity must be greater than zero")
    private Integer requiredQuantity;

    @NotNull(message = "Approx piece weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be greater than zero")
    private BigDecimal approxPieceWeightKg;

    private Boolean machineRequired;

    @NotNull(message = "Pattern source is required")
    private PatternProvidedBy patternProvidedBy;
}