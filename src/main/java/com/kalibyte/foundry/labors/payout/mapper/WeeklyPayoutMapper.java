package com.kalibyte.foundry.labors.payout.mapper;

import com.kalibyte.foundry.labors.payout.dto.WeeklyPayoutResponse;
import com.kalibyte.foundry.labors.payout.entity.WeeklyPayout;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WeeklyPayoutMapper {

    @Mapping(target = "laborerId", source = "laborer.id")
    @Mapping(target = "laborerName", source = "laborer.name")
    WeeklyPayoutResponse toResponse(WeeklyPayout entity);
}
