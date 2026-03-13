package com.kalibyte.foundry.inventory.inward.mapper;

import com.kalibyte.foundry.inventory.inward.dto.response.*;
import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.inward.entity.ReceivedItem;
import com.kalibyte.foundry.inventory.inward.entity.enums.ReceiptStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

/**
 * MapStruct mapper for MaterialInward entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InwardMapper {

    @Mapping(source = "purchaseOrder.poNumber", target = "poNumber")
    @Mapping(source = "vendor.name", target = "vendorName")
    @Mapping(target = "totalAmount", expression = "java(inward.getTotalAmount())")
    InwardResponse toResponse(MaterialInward inward);

    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.name", target = "itemName")
    @Mapping(source = "item.code", target = "itemCode")
    @Mapping(target = "unit", expression = "java(item.getItem().getUnit().name())")
    @Mapping(target = "amount", expression = "java(item.getAmount())")
    ReceivedItemDetail toDetail(ReceivedItem item);

    @Mapping(source = "vendor.name", target = "vendorName")
    @Mapping(target = "totalItems", expression = "java(inward.getReceivedItems().size())")
    @Mapping(target = "totalAmount", expression = "java(inward.getTotalAmount())")
    InwardSummary toSummary(MaterialInward inward);

    @Mapping(source = "id", target = "inwardId")
    @Mapping(source = "purchaseOrder.poNumber", target = "purchaseOrderNumber")
    @Mapping(source = "vendor.name", target = "vendorName")
    @Mapping(target = "totalAmount", expression = "java(inward.getTotalAmount())")
    @Mapping(target = "hasShortage", expression = "java(hasShortage(inward))")
    @Mapping(target = "hasExcess", expression = "java(hasExcess(inward))")
    InwardReviewResponse toReviewResponse(MaterialInward inward);

    @Mapping(source = "item.name", target = "itemName")
    @Mapping(source = "item.code", target = "itemCode")
    @Mapping(target = "unit", expression = "java(item.getItem().getUnit().name())")
    @Mapping(target = "orderedQuantity", expression = "java(item.getPoQuantity() != null ? item.getPoQuantity() : BigDecimal.ZERO)")
    @Mapping(target = "quantityDifference", expression = "java(item.getQuantityDifference())")
    @Mapping(target = "receiptStatus", expression = "java(item.getReceiptStatus().name())")
    @Mapping(target = "totalAmount", expression = "java(item.getAmount())")
    ReceivedItemComparison toComparison(ReceivedItem item);

    default boolean hasShortage(MaterialInward inward) {
        return inward.getReceivedItems().stream()
                .anyMatch(item -> item.getReceiptStatus() == ReceiptStatus.SHORT);
    }

    default boolean hasExcess(MaterialInward inward) {
        return inward.getReceivedItems().stream()
                .anyMatch(item -> item.getReceiptStatus() == ReceiptStatus.EXCESS);
    }
}
