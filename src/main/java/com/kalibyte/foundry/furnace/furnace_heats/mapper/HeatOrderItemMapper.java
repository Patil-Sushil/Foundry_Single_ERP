package com.kalibyte.foundry.furnace.furnace_heats.mapper;

import com.kalibyte.foundry.furnace.furnace_heats.dto.request.HeatOrderItemRequest;
import com.kalibyte.foundry.furnace.furnace_heats.dto.response.HeatOrderItemResponse;
import com.kalibyte.foundry.furnace.furnace_heats.entity.HeatOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HeatOrderItemMapper {
    @Mapping(source = "orderItem.id", target = "orderItemId")
    @Mapping(source = "orderItem.partName", target = "partName")
    @Mapping(source = "orderItem.drawingNumber", target = "drawingNumber")
    HeatOrderItemResponse toResponse(HeatOrderItem item);

    @Mapping(target = "orderItem", ignore = true)
    @Mapping(target = "heat", ignore = true)
    @Mapping(target = "id", ignore = true)
    HeatOrderItem toEntity(HeatOrderItemRequest request);

    @Mapping(target = "orderItem", ignore = true)
    @Mapping(target = "heat", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntity(HeatOrderItemRequest request, @MappingTarget HeatOrderItem item);
}
