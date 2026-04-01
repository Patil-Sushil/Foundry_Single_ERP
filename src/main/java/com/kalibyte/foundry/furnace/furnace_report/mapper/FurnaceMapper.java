package com.kalibyte.foundry.furnace.furnace_report.mapper;

import com.kalibyte.foundry.furnace.furnace_heats.mapper.FurnaceHeatMapper;
import com.kalibyte.foundry.furnace.furnace_report.dto.Request.FurnaceRequestDTO;
import com.kalibyte.foundry.furnace.furnace_report.dto.response.FurnaceResponseDTO;
import com.kalibyte.foundry.furnace.furnace_report.entity.Furnace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {FurnaceHeatMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FurnaceMapper {
    FurnaceResponseDTO toResponse(Furnace furnace);

    @Mapping(target = "heats", ignore = true)
    @Mapping(target = "furnaceRefNo", ignore = true)
    Furnace toEntity(FurnaceRequestDTO request);

    @Mapping(target = "heats", ignore = true)
    @Mapping(target = "furnaceRefNo", ignore = true)
    void updateEntity(FurnaceRequestDTO request, @MappingTarget Furnace furnace);
}
