package com.kalibyte.foundry.order.dto.request;

import com.kalibyte.foundry.order.entity.enums.PaymentTerms;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {

    private UUID quotationId;

    private UUID customerId;

    @NotNull(message = "Delivery date is required")
    private LocalDate deliveryDate;

    private String placeOfSupply;
    private String poReference;

    // Payment Terms
    private PaymentTerms paymentTerms;
    private String customPaymentTerms;

    // Default GST percentage for the order (can be overridden per item)
    private BigDecimal gstPercentage;

    @Valid
    private List<OrderItemRequest> items;
}