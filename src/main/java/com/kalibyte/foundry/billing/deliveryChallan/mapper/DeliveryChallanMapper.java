package com.kalibyte.foundry.billing.deliveryChallan.mapper;

import com.kalibyte.foundry.billing.deliveryChallan.dto.response.DeliveryChallanItemResponse;
import com.kalibyte.foundry.billing.deliveryChallan.dto.response.DeliveryChallanResponse;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallan;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallanItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DeliveryChallanMapper {

    // =========================================================
    //  DELIVERY CHALLAN -> RESPONSE
    // =========================================================

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "items", source = "items")
    DeliveryChallanResponse toResponse(DeliveryChallan dc);

    List<DeliveryChallanResponse> toResponseList(List<DeliveryChallan> dcList);

    // =========================================================
    //  DELIVERY CHALLAN ITEM -> ITEM RESPONSE
    // =========================================================

    @Mapping(target = "castingName", source = "orderItem.partName")
    @Mapping(target = "patternName", source = "orderItem.pattern.patternName")
    DeliveryChallanItemResponse toItemResponse(DeliveryChallanItem item);

    List<DeliveryChallanItemResponse> toItemResponseList(List<DeliveryChallanItem> items);
}