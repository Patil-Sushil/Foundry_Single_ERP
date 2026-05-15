package com.kalibyte.foundry.labors.labor.mapper;

import com.kalibyte.foundry.labors.labor.dto.LaborerRequest;
import com.kalibyte.foundry.labors.labor.dto.LaborerResponse;
import com.kalibyte.foundry.labors.labor.entity.Laborer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LaborerMapper {

	@Mapping(target = "id", ignore = true)
	Laborer toEntity(LaborerRequest request);

	@Mapping(target = "id", ignore = true)
	void updateEntityFromDto(LaborerRequest request, @org.mapstruct.MappingTarget Laborer entity);

	@Mapping(source = "phNumber", target = "phNumber")
	@Mapping(source = "email", target = "email")
	@Mapping(source = "address", target = "address")
	LaborerResponse toResponse(Laborer entity);

	List<LaborerResponse> toResponseDTOList(List<Laborer> laborers);

}
