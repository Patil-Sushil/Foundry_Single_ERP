package com.kalibyte.foundry.scrap.mapper;

import com.kalibyte.foundry.scrap.dto.request.ScrapEntryRequest;
import com.kalibyte.foundry.scrap.dto.response.ScrapEntryResponse;
import com.kalibyte.foundry.scrap.dto.response.ScrapItemResponse;
import com.kalibyte.foundry.scrap.entity.ScrapEntry;
import com.kalibyte.foundry.scrap.entity.ScrapItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScrapMapper {
    ScrapEntryResponse toResponse(ScrapEntry entry);

    ScrapItemResponse toItemResponse(ScrapItem item);

    @Mapping(target = "scrapItems", ignore = true)
    @Mapping(target = "id", ignore = true)
    ScrapEntry toEntity(ScrapEntryRequest request);

    @Mapping(target = "scrapItems", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntity(ScrapEntryRequest request, @MappingTarget ScrapEntry entry);

    List<ScrapEntryResponse> toResponseList(List<ScrapEntry> response);
}
