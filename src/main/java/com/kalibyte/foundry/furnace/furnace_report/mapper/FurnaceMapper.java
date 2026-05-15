package com.kalibyte.foundry.furnace.furnace_report.mapper;

import com.kalibyte.foundry.furnace.furnace_heats.mapper.FurnaceHeatMapper;
import com.kalibyte.foundry.furnace.furnace_report.dto.Request.FurnaceRequest;
import com.kalibyte.foundry.furnace.furnace_report.dto.response.FurnaceResponse;
import com.kalibyte.foundry.furnace.furnace_report.entity.Furnace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {FurnaceHeatMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FurnaceMapper {
    FurnaceResponse toResponse(Furnace furnace);

    @Mapping(target = "heats", ignore = true)
    @Mapping(target = "furnaceRefNo", ignore = true)
    Furnace toEntity(FurnaceRequest request);

    @Mapping(target = "heats", ignore = true)
    @Mapping(target = "furnaceRefNo", ignore = true)
    void updateEntity(FurnaceRequest request, @MappingTarget Furnace furnace);
}
