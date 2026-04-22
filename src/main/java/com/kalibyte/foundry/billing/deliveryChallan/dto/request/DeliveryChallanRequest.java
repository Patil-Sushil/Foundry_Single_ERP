package com.kalibyte.foundry.billing.deliveryChallan.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryChallanRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Dispatch date is required")
    private LocalDate dispatchDate;

    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;

    @NotBlank(message = "Transport name is required")
    private String transportName;

    private String lrNumber;

    @NotEmpty(message = "Items list cannot be empty")
    private List<@Valid DeliveryChallanItemRequest> items;

}
