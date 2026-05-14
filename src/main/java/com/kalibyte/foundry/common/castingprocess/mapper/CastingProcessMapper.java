package com.kalibyte.foundry.common.castingprocess.mapper;

import com.kalibyte.foundry.common.castingprocess.dto.CastingProcessRequest;
import com.kalibyte.foundry.common.castingprocess.dto.CastingProcessResponse;
import com.kalibyte.foundry.common.castingprocess.entity.CastingProcessMaster;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Builder;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CastingProcessMapper {

    CastingProcessResponse toResponse(CastingProcessMaster entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CastingProcessMaster toEntity(CastingProcessRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void update(@MappingTarget CastingProcessMaster entity, CastingProcessRequest request);
}
