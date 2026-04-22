package com.kalibyte.foundry.billing.invoice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

//    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;

    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

}
