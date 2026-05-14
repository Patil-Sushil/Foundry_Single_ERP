package com.kalibyte.foundry.production.dto.response.report.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DelayedOrderResponse {
    private UUID orderId;
    private String orderNumber;
    private String customerName;
    private LocalDate deliveryDate;
    private LocalDate expectedCompletionDate;
    private Long delayDays;
    private Double completionPercentage;
}
