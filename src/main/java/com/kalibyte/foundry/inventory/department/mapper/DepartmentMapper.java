package com.kalibyte.foundry.inventory.department.mapper;

import com.kalibyte.foundry.inventory.department.dto.request.DepartmentRequest;
import com.kalibyte.foundry.inventory.department.dto.response.DepartmentResponse;
import com.kalibyte.foundry.inventory.department.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for Department entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentMapper {

    /**
     * Maps Department entity to DepartmentResponse DTO.
     */
    DepartmentResponse toResponse(Department department);

    /**
     * Maps DepartmentRequest DTO to Department entity.
     */
    Department toEntity(DepartmentRequest request);
}
