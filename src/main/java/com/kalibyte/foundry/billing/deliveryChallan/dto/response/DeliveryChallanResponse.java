package com.kalibyte.foundry.billing.deliveryChallan.dto.response;


import com.kalibyte.foundry.billing.enums.DCStatus;
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
public class DeliveryChallanResponse {

    private UUID id;

    private String dcNumber;

    private UUID orderId;

    private UUID customerId;

    private LocalDate dispatchDate;

    private String vehicleNumber;

    private String transportName;

    private String lrNumber;

    private Integer totalQuantity;

    private BigDecimal totalWeight;

    private BigDecimal totalAmount;

    private DCStatus status;

    private List<DeliveryChallanItemResponse> items;

}
