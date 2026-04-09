package com.kalibyte.foundry.labors.labor.mapper;

import com.kalibyte.foundry.labors.labor.dto.LaborerRequestDTO;
import com.kalibyte.foundry.labors.labor.dto.LaborerResponseDTO;
import com.kalibyte.foundry.labors.labor.entity.Laborer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LaborerMapper {

	@Mapping(target = "id", ignore = true)
	Laborer toEntity(LaborerRequestDTO request);

	@Mapping(target = "id", ignore = true)
	void updateEntityFromDto(LaborerRequestDTO request, @org.mapstruct.MappingTarget Laborer entity);

	@Mapping(source = "phNumber", target = "phNumber")
	@Mapping(source = "email", target = "email")
	@Mapping(source = "address", target = "address")
	LaborerResponseDTO toResponse(Laborer entity);

	List<LaborerResponseDTO> toResponseDTOList(List<Laborer> laborers);

}
