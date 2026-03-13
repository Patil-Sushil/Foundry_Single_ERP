package com.kalibyte.foundry.inventory.purchaseorder.mapper;

import com.kalibyte.foundry.inventory.purchaseorder.dto.request.CreatePurchaseOrderRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.LastPurchaseRate;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.OrderItemDetail;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderResponse;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderSummary;
import com.kalibyte.foundry.inventory.purchaseorder.entity.ItemVendorRate;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrderItem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for Purchase Order entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseOrderMapper {

    @Mapping(source = "vendor.name", target = "vendorName")
    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "orderItems", target = "items")
    @Mapping(target = "totalOrderValue", expression = "java(po.getTotalOrderValue())")
    PurchaseOrderResponse toResponse(PurchaseOrder po);

    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.name", target = "itemName")
    @Mapping(source = "item.code", target = "itemCode")
    @Mapping(source = "item.unit", target = "unit")
    @Mapping(target = "pendingQuantity", expression = "java(item.getPendingQuantity())")
    @Mapping(target = "totalValue", expression = "java(item.getTotalValue())")
    OrderItemDetail toItemDetail(PurchaseOrderItem item);

    @Mapping(source = "vendor.name", target = "vendorName")
    @Mapping(target = "totalItems", expression = "java(po.getOrderItems().size())")
    @Mapping(target = "totalOrderValue", expression = "java(po.getTotalOrderValue())")
    PurchaseOrderSummary toSummary(PurchaseOrder po);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "poNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "poDate", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    PurchaseOrder toEntity(CreatePurchaseOrderRequest request);

    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "lastRate", target = "rate")
    LastPurchaseRate toLastPurchaseRate(ItemVendorRate rate);
}
