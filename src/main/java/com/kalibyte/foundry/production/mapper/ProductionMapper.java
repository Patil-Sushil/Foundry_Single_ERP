package com.kalibyte.foundry.production.mapper;

import com.kalibyte.foundry.production.dto.response.entry.ProductionEntryResponse;
import com.kalibyte.foundry.production.dto.response.entry.ProductionItemResponse;
import com.kalibyte.foundry.production.entity.ProductionEntry;
import com.kalibyte.foundry.production.entity.ProductionItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductionMapper {

    //------------------------------------------------
    // ENTRY → RESPONSE
    //------------------------------------------------

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.orderNumber", target = "orderNumber")
    @Mapping(source = "productionItems", target = "items")
    ProductionEntryResponse toResponse(ProductionEntry entry);

    List<ProductionEntryResponse> toResponseList(List<ProductionEntry> entries);

    //------------------------------------------------
    // ITEM → RESPONSE
    //------------------------------------------------

    @Mapping(source = "orderItem.id", target = "orderItemId")
    @Mapping(source = "heatOrderItem.id", target = "heatOrderItemId")
    ProductionItemResponse toItemResponse(ProductionItem item);

    List<ProductionItemResponse> toItemResponseList(List<ProductionItem> items);

    //------------------------------------------------
    // PARTIAL UPDATE SUPPORT
    //------------------------------------------------

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntryFromEntity(ProductionEntry source, @MappingTarget ProductionEntry target);
}
