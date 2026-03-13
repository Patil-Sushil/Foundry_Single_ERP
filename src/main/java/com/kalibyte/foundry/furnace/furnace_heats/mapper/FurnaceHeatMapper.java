package com.kalibyte.foundry.furnace.furnace_heats.mapper;

import com.kalibyte.foundry.furnace.furnace_heats.dto.request.FurnaceHeatRequest;
import com.kalibyte.foundry.furnace.furnace_heats.dto.response.FurnaceHeatResponse;
import com.kalibyte.foundry.furnace.furnace_heats.entity.FurnaceHeats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {HeatMaterialMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FurnaceHeatMapper {
    @Mapping(source = "order.id", target = "orderId")
    FurnaceHeatResponse toResponse(FurnaceHeats heat);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "materialsUsed", ignore = true)
    @Mapping(target = "furnace", ignore = true)
    @Mapping(target = "id", ignore = true)
    FurnaceHeats toEntity(FurnaceHeatRequest request);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "materialsUsed", ignore = true)
    @Mapping(target = "furnace", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntity(FurnaceHeatRequest request, @MappingTarget FurnaceHeats heat);
}
