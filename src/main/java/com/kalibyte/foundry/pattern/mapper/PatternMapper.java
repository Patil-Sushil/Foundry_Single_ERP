package com.kalibyte.foundry.pattern.mapper;

import com.kalibyte.foundry.pattern.dto.response.PatternResponse;
import com.kalibyte.foundry.pattern.entity.Pattern;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PatternMapper {

    @Mapping(target = "name", source = "patternName")
    PatternResponse toResponse(Pattern pattern);
}
