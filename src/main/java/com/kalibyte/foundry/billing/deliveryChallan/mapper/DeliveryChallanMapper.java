package com.kalibyte.foundry.billing.deliveryChallan.mapper;

import com.kalibyte.foundry.billing.deliveryChallan.dto.request.DeliveryChallanItemRequest;
import com.kalibyte.foundry.billing.deliveryChallan.dto.response.DeliveryChallanItemResponse;
import com.kalibyte.foundry.billing.deliveryChallan.dto.response.DeliveryChallanResponse;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallan;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallanItem;
import com.kalibyte.foundry.order.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class DeliveryChallanMapper {

    //------------------------------------------------
    // REQUEST → ENTITY
    //------------------------------------------------

    public static DeliveryChallanItem toItemEntity(DeliveryChallanItemRequest request, DeliveryChallan dc) {

        BigDecimal amount = request.getWeight().multiply(request.getRate());

        return DeliveryChallanItem.builder()
                .deliveryChallan(dc)
                .orderItem(request.getOrderItem()) // make sure this exists in request
                .quantity(request.getQuantity())
                .weight(request.getWeight())
                .rate(request.getRate())
                .amount(amount)
                .build();
    }

    //------------------------------------------------
    // ENTITY → RESPONSE
    //------------------------------------------------

    public static DeliveryChallanItemResponse toItemResponse(DeliveryChallanItem item) {

        OrderItem orderItem = item.getOrderItem();

        String castingName = null;

        if (orderItem != null) {
            castingName = orderItem.getPartName();  // or getCastingName()
        }

        return DeliveryChallanItemResponse.builder()
                .castingName(castingName)
                .quantity(item.getQuantity())
                .weight(item.getWeight())
                .rate(item.getRate())
                .amount(item.getAmount())
                .build();
    }

    //------------------------------------------------
    // DELIVERY CHALLAN → RESPONSE
    //------------------------------------------------

    public static DeliveryChallanResponse toResponse(DeliveryChallan dc) {

        List<DeliveryChallanItemResponse> items =
                dc.getItems()
                        .stream()
                        .map(DeliveryChallanMapper::toItemResponse)
                        .collect(Collectors.toList());

        return DeliveryChallanResponse.builder()
                .id(dc.getId())
                .dcNumber(dc.getDcNumber())
                .orderId(dc.getOrder().getId())
                .customerId(dc.getCustomer().getId())
                .dispatchDate(dc.getDispatchDate())
                .vehicleNumber(dc.getVehicleNumber())
                .transportName(dc.getTransportName())
                .lrNumber(dc.getLrNumber())
                .totalQuantity(dc.getTotalQuantity())
                .totalWeight(dc.getTotalWeight())
                .totalAmount(dc.getTotalAmount())
                .status(dc.getStatus())
                .items(items)
                .build();
    }
}