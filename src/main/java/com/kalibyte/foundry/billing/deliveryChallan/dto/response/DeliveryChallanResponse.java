package com.kalibyte.foundry.billing.deliveryChallan.dto.response;

import com.kalibyte.foundry.billing.deliveryChallan.entity.enums.DCStatus;
import com.kalibyte.foundry.order.entity.enums.GstType;
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

    // GST breakdown
    private GstType gstType;
    private BigDecimal gstPercentage;
    private BigDecimal subtotal;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalGst;

    private BigDecimal totalAmount;

    private DCStatus status;

    private List<DeliveryChallanItemResponse> items;
}