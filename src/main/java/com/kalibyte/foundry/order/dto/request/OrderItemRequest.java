package com.kalibyte.foundry.order.dto.request;

import com.kalibyte.foundry.enquiry.entity.enums.MetalCategory;
import com.kalibyte.foundry.enquiry.entity.enums.MetalType;
import com.kalibyte.foundry.pattern.dto.request.PatternReceiptRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    @NotBlank(message = "Part name is required")
    private String partName;

    private String materialGrade;

    // Metal & Casting
    private MetalType metalType;
    private MetalCategory metalCategory;
    private UUID castingProcessId;

    private Boolean isMachiningRequired;

    @NotNull(message = "Net weight is required")
    @DecimalMin(value = "0.001", message = "Net weight must be greater than 0")
    private BigDecimal netWeightKg;

    private BigDecimal grossWeightKg;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

//    private BigDecimal discount;


    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    // GST percentage per item (defaults to 18 if not provided)
    private BigDecimal gstPercentage;

    @NotNull(message = "Pattern source is required")
    private Boolean patternProvidedByCustomer;

    private UUID patternId;

    @Valid
    private PatternReceiptRequest patternReceipt;
}