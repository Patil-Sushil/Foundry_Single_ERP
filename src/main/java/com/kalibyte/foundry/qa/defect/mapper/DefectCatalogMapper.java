package com.kalibyte.foundry.qa.defect.mapper;

import com.kalibyte.foundry.qa.defect.dto.DefectCatalogRequest;
import com.kalibyte.foundry.qa.defect.dto.DefectCatalogResponse;
import com.kalibyte.foundry.qa.defect.entity.DefectCatalog;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface DefectCatalogMapper {

    @Mapping(target = "id", ignore = true)
    DefectCatalog toEntity(DefectCatalogRequest request);

    DefectCatalogResponse toResponse(DefectCatalog entity);

    List<DefectCatalogResponse> toResponseList(List<DefectCatalog> list);
}
