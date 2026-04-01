package com.kalibyte.foundry.order.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private UUID id;

    // Order reference (populated when fetching items across orders)
    private UUID orderId;
    private String orderNumber;
    private String orderStatus;

    // Customer reference
    private UUID customerId;
    private String customerName;

    private String partName;
    private String drawingNumber;
    private String materialGrade;

    // Metal & Casting
    private String metalType;
    private String metalCategory;
    private String castingProcess;

    private BigDecimal netWeightKg;
    private BigDecimal grossWeightKg;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    // GST per item
    private BigDecimal gstPercentage;
    private BigDecimal gstAmount;
    private BigDecimal totalWithGst;

    private Boolean patternProvidedByCustomer;
    private String patternNumber;
    private String patternName;

    private String receiptName;
    private String receiptType;
    private String receiptMaterial;

    private Integer producedQuantity;
    private Integer dispatchedQuantity;
    private Integer pendingQuantity;
}