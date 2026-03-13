package com.kalibyte.foundry.inventory.issue.mapper;

import com.kalibyte.foundry.inventory.issue.dto.request.RecordIssueRequest;
import com.kalibyte.foundry.inventory.issue.dto.response.*;
import com.kalibyte.foundry.inventory.issue.entity.IssuedItem;
import com.kalibyte.foundry.inventory.issue.entity.MaterialIssue;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MapStruct mapper for MaterialIssue entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MaterialIssueMapper {

    @Mapping(source = "department.name", target = "departmentName")
    @Mapping(source = "issuedItems", target = "items")
    @Mapping(target = "totalValue", expression = "java(issue.getTotalValue())")
    MaterialIssueResponse toResponse(MaterialIssue issue);

    @Mapping(source = "item.name", target = "itemName")
    @Mapping(source = "item.code", target = "itemCode")
    @Mapping(target = "unit", expression = "java(issuedItem.getItem().getUnit().name())")
    @Mapping(target = "amount", expression = "java(issuedItem.getAmount())")
    IssuedItemDetail toDetail(IssuedItem issuedItem);

    @Mapping(source = "department.name", target = "departmentName")
    @Mapping(target = "totalItems", expression = "java(issue.getIssuedItems().size())")
    @Mapping(target = "totalValue", expression = "java(issue.getTotalValue())")
    MaterialIssueSummary toSummary(MaterialIssue issue);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "issueNumber", ignore = true)
    @Mapping(target = "issuedByUserId", ignore = true)
    @Mapping(target = "issuedItems", ignore = true)
    @Mapping(target = "issueDate", expression = "java(request.issueDate() != null ? request.issueDate() : java.time.LocalDate.now())")
    MaterialIssue toEntity(RecordIssueRequest request);


    /**
     * Aggregates consumption data from a list of material issues into a report.
     */
    default DepartmentConsumptionReport toConsumptionReport(Long departmentId, LocalDate fromDate, LocalDate toDate, List<MaterialIssue> issues) {
        Map<Long, ConsumptionDetail> map = new HashMap<>();

        for (MaterialIssue issue : issues) {
            for (IssuedItem ii : issue.getIssuedItems()) {
                Long itemId = ii.getItem().getId();
                ConsumptionDetail existing = map.get(itemId);
                
                if (existing == null) {
                    map.put(itemId, new ConsumptionDetail(
                            ii.getItem().getName(),
                            ii.getItem().getCode(),
                            ii.getItem().getUnit().name(),
                            ii.getIssuedQuantity(),
                            ii.getAmount()
                    ));
                } else {
                    map.put(itemId, new ConsumptionDetail(
                            existing.itemName(),
                            existing.itemCode(),
                            existing.unit(),
                            existing.totalQuantity().add(ii.getIssuedQuantity()),
                            existing.totalValue().add(ii.getAmount())
                    ));
                }
            }
        }

        List<ConsumptionDetail> items = new ArrayList<>(map.values());
        BigDecimal grandTotal = items.stream()
                .map(ConsumptionDetail::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DepartmentConsumptionReport(departmentId, fromDate, toDate, items, grandTotal);
    }
}
