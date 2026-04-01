package com.kalibyte.foundry.qa.rejection.mapper;

import com.kalibyte.foundry.qa.rejection.dto.QaRejectionResponse;
import com.kalibyte.foundry.qa.rejection.entity.QaRejection;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface QaRejectionMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "orderItemId", source = "orderItem.id")
    @Mapping(target = "itemName", source = "orderItem.partName")
    @Mapping(target = "inspectionId", source = "inspection.id")
    @Mapping(target = "inspectionNumber", source = "inspection.inspectionNumber")
    @Mapping(target = "primaryDefectId", source = "primaryDefect.id")
    @Mapping(target = "primaryDefectName", source = "primaryDefect.name")
    QaRejectionResponse toResponse(QaRejection entity);

    List<QaRejectionResponse> toResponseList(List<QaRejection> list);
}
