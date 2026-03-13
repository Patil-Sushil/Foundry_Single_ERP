package com.kalibyte.foundry.inventory.item.mapper;

import com.kalibyte.foundry.inventory.item.dto.response.ItemResponse;
import com.kalibyte.foundry.inventory.item.dto.response.ItemSummary;
import com.kalibyte.foundry.inventory.item.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for Item entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemMapper {

    /**
     * Maps Item entity to ItemResponse DTO.
     * Automatically handles null-checks for department.
     */
    @Mapping(source = "department.name", target = "departmentName")
    ItemResponse toResponse(Item item);

    /**
     * Maps Item entity to ItemSummary DTO for lists and search results.
     */
    ItemSummary toSummary(Item item);
}
