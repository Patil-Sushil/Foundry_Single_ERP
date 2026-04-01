package com.kalibyte.foundry.billing.deliveryChallan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchAvailableResponse {
    private UUID orderItemId;
    private Integer orderedQuantity;
    private Integer totalAccepted;
    private Integer alreadyDispatched;
    private Integer availableForDispatch;
}
