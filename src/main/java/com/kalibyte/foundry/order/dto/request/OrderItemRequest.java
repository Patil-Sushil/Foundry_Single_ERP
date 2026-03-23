package com.kalibyte.foundry.order.dto.request;

import com.kalibyte.foundry.pattern.dto.request.PatternReceiptRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class OrderItemRequest {

    //------------------------------------------------
    // PART INFO
    //------------------------------------------------
    @NotBlank(message = "Part name is required")
    private String partName;

    @NotBlank(message = "Material grade is required")
    private String materialGrade;

    //------------------------------------------------
    // WEIGHT (IMPORTANT)
    //------------------------------------------------
    @NotNull(message = "Net weight is required")
    private BigDecimal netWeightKg;

    //------------------------------------------------
    // QUANTITY
    //------------------------------------------------
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    //------------------------------------------------
    // PRICING
    //------------------------------------------------
    @NotNull(message = "Unit price is required")
    private BigDecimal unitPrice;

    //------------------------------------------------
    // PATTERN LOGIC
    //------------------------------------------------
    @NotNull(message = "Pattern source (customer/company) must be specified")
    private Boolean patternProvidedByCustomer;

    // If company pattern
    private UUID patternId;

    // If customer pattern
    @Valid
    private PatternReceiptRequest patternReceipt;
}