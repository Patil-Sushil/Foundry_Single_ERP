package com.kalibyte.foundry.furnace.furnace_heats.mapper;

import com.kalibyte.foundry.furnace.furnace_heats.dto.response.HeatMaterialItemResponse;
import com.kalibyte.foundry.furnace.furnace_heats.entity.HeatMaterialItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HeatMaterialMapper {
    HeatMaterialItemResponse toResponse(HeatMaterialItem material);
}
