package com.kalibyte.foundry.furnace.furnace_heats.mapper;

import com.kalibyte.foundry.furnace.furnace_heats.dto.request.FurnaceHeatRequest;
import com.kalibyte.foundry.furnace.furnace_heats.dto.response.FurnaceHeatResponse;
import com.kalibyte.foundry.furnace.furnace_heats.entity.FurnaceHeats;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {HeatMaterialMapper.class, HeatOrderItemMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FurnaceHeatMapper {
    @Mapping(source = "order.id", target = "orderId")
    FurnaceHeatResponse toResponse(FurnaceHeats heat);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "materialsUsed", ignore = true)
    @Mapping(target = "heatOrderItems", ignore = true)
    @Mapping(target = "furnace", ignore = true)
    @Mapping(target = "id", ignore = true)
    FurnaceHeats toEntity(FurnaceHeatRequest request);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "materialsUsed", ignore = true)
    @Mapping(target = "heatOrderItems", ignore = true)
    @Mapping(target = "furnace", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntity(FurnaceHeatRequest request, @MappingTarget FurnaceHeats heat);

    @AfterMapping
    default void calculateDerivedFields(FurnaceHeats entity, @MappingTarget FurnaceHeatResponse response) {
        if (entity.getLiquidMetalWeight() != null && entity.getLiquidMetalWeight().compareTo(BigDecimal.ZERO) > 0) {
            // Metal loss
            response.setMetalLoss(entity.getMetalLoss());

            // Yield percentage = (castings / liquid metal) * 100
            BigDecimal castings = entity.getCastingsPouredWeight() != null
                    ? entity.getCastingsPouredWeight() : BigDecimal.ZERO;
            BigDecimal yield = castings
                    .multiply(BigDecimal.valueOf(100))
                    .divide(entity.getLiquidMetalWeight(), 2, java.math.RoundingMode.HALF_UP);
            response.setYieldPercentage(yield);

            // Remaining capacity
            response.setRemainingCapacity(entity.getRemainingCastingsCapacity());
        }
    }
}
