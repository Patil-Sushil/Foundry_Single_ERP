package com.kalibyte.foundry.enquiry.dto.request;

import com.kalibyte.foundry.enquiry.entity.enums.MetalType;
import com.kalibyte.foundry.pattern.dto.request.PatternReceiptRequest;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

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

    @NotBlank(message = "Casting process is required")
    private String castingProcess;

    @NotNull(message = "Required quantity is mandatory")
    @Min(value = 1, message = "Quantity must be greater than zero")
    private Integer requiredQuantity;

    @NotNull(message = "Approx piece weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be greater than zero")
    private BigDecimal approxPieceWeightKg;

    private Boolean machineRequired;

    @NotNull(message = "Pattern provided flag is required")
    private Boolean patternProvidedByCustomer;

    // Used when patternProvidedByCustomer = false
    private UUID patternId;

    // Used when patternProvidedByCustomer = true
    private PatternReceiptRequest patternReceipt;
}


