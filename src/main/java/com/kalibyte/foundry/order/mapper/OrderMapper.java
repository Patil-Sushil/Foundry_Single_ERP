package com.kalibyte.foundry.order.mapper;

import com.kalibyte.foundry.order.dto.response.OrderItemResponse;
import com.kalibyte.foundry.order.dto.response.OrderResponse;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "quotation.id", target = "quotationId")
    @Mapping(source = "quotation.quotationNumber", target = "quotationNumber")
    @Mapping(target = "totalQuantity", ignore = true)
    @Mapping(target = "producedQuantity", ignore = true)
    @Mapping(target = "dispatchedQuantity", ignore = true)
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);

    @Mapping(source = "metalType.displayName", target = "metalType")
    @Mapping(target = "metalCategory", ignore = true)
    @Mapping(target = "patternNumber", ignore = true)
    @Mapping(target = "patternName", ignore = true)
    @Mapping(target = "receiptName", ignore = true)
    @Mapping(target = "receiptType", ignore = true)
    @Mapping(target = "receiptMaterial", ignore = true)
    @Mapping(target = "pendingQuantity", expression = "java(item.getPendingQuantity())")
    OrderItemResponse toItemResponse(OrderItem item);

    List<OrderItemResponse> toItemResponseList(List<OrderItem> items);

    @AfterMapping
    default void mapExtraDetails(OrderItem item, @MappingTarget OrderItemResponse response) {
        if (item == null) return;

        // Metal Category
        if (item.getMetalType() != null && item.getMetalType().getCategory() != null) {
            response.setMetalCategory(item.getMetalType().getCategory().getDisplayName());
        }

        // Pattern Details
        if (Boolean.TRUE.equals(item.getPatternProvidedByCustomer())) {
            if (item.getPatternReceipt() != null) {
                response.setReceiptName(item.getPatternReceipt().getName());
                response.setReceiptType(item.getPatternReceipt().getType() != null ?
                        item.getPatternReceipt().getType().name() : null);
                response.setReceiptMaterial(item.getPatternReceipt().getMaterial() != null ?
                        item.getPatternReceipt().getMaterial().name() : null);
            }
        } else {
            if (item.getPattern() != null) {
                response.setPatternNumber(item.getPattern().getPatternNumber());
                response.setPatternName(item.getPattern().getName());
            }
        }
    }

    @AfterMapping
    default void calculateSummary(Order order, @MappingTarget OrderResponse response) {
        if (order.getItems() == null || order.getItems().isEmpty()) return;

        int totalQty = 0;
        int producedQty = 0;
        int dispatchedQty = 0;

        for (OrderItem item : order.getItems()) {
            totalQty += item.getQuantity() != null ? item.getQuantity() : 0;
            producedQty += item.getProducedQuantity() != null ? item.getProducedQuantity() : 0;
            dispatchedQty += item.getDispatchedQuantity() != null ? item.getDispatchedQuantity() : 0;
        }

        response.setTotalQuantity(totalQty);
        response.setProducedQuantity(producedQty);
        response.setDispatchedQuantity(dispatchedQty);
    }
}