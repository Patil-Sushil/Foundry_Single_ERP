package com.kalibyte.foundry.qa.inspection.mapper;

import com.kalibyte.foundry.qa.inspection.dto.FindingRequest;
import com.kalibyte.foundry.qa.inspection.dto.FindingResponse;
import com.kalibyte.foundry.qa.inspection.dto.QaInspectionRequest;
import com.kalibyte.foundry.qa.inspection.dto.QaInspectionResponse;
import com.kalibyte.foundry.qa.inspection.entity.InspectionFinding;
import com.kalibyte.foundry.qa.inspection.entity.QaInspection;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface QaInspectionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "findings", ignore = true)
    @Mapping(target = "productionEntry.id", source = "productionEntryId")
    @Mapping(target = "productionItem.id", source = "productionItemId")
    @Mapping(target = "order.id", source = "orderId")
    @Mapping(target = "orderItem.id", source = "orderItemId")
    @Mapping(target = "heatOrderItem.id", source = "heatOrderItemId")
    QaInspection toEntity(QaInspectionRequest request);

    @Mapping(target = "productionEntryId", source = "productionEntry.id")
    @Mapping(target = "productionItemId", source = "productionItem.id")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderItemId", source = "orderItem.id")
    @Mapping(target = "heatOrderItemId", source = "heatOrderItem.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "itemName", source = "orderItem.partName")
    QaInspectionResponse toResponse(QaInspection entity);

    @Mapping(target = "defectId", source = "defect.id")
    @Mapping(target = "defectCode", source = "defect.code")
    @Mapping(target = "defectName", source = "defect.name")
    FindingResponse toFindingResponse(InspectionFinding finding);

    @Mapping(target = "defect.id", source = "defectId")
    InspectionFinding toFindingEntity(FindingRequest request);

    List<QaInspectionResponse> toResponseList(List<QaInspection> list);
}
