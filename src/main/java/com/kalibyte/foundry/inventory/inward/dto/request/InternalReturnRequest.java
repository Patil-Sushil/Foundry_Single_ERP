package com.kalibyte.foundry.inventory.inward.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalReturnRequest {
    private String vehicleNumber;
    private String driverName;
    private String remarks;
    private LocalDate returnDate;
    private Long scrapEntryId;
    private List<InternalReturnItemRequest> items;

    public record InternalReturnItemRequest(
        Long itemId,
        BigDecimal quantity,
        BigDecimal unitRate,
        String reason
    ) {}
}
