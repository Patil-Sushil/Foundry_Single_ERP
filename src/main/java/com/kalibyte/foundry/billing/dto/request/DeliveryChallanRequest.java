package com.kalibyte.foundry.billing.dto.request;

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

    private UUID orderId;

    private UUID customerId;

    private LocalDate dispatchDate;

    private String vehicleNumber;

    private String transportName;

    private String lrNumber;

    private List<DeliveryChallanItemRequest> items;

}
