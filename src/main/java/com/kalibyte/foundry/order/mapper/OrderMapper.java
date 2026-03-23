package com.kalibyte.foundry.order.mapper;

import com.kalibyte.foundry.order.dto.response.OrderItemResponse;
import com.kalibyte.foundry.order.dto.response.OrderResponse;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    //------------------------------------------------
    // ORDER → RESPONSE
    //------------------------------------------------
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "items", target = "items")
    OrderResponse toResponse(Order order);

    //------------------------------------------------
    // ITEM → RESPONSE
    //------------------------------------------------
    @Mapping(source = "id", target = "id")
    @Mapping(source = "partName", target = "partName")
    @Mapping(source = "materialGrade", target = "materialGrade")
    @Mapping(source = "lineTotal", target = "lineTotal")
    OrderItemResponse toItemResponse(OrderItem item);

    List<OrderItemResponse> toItemResponses(List<OrderItem> items);

    //------------------------------------------------
    // PATTERN LOGIC
    //------------------------------------------------
    @AfterMapping
    default void mapPattern(OrderItem item,
                            @MappingTarget OrderItemResponse res) {

        if (Boolean.TRUE.equals(item.getPatternProvidedByCustomer())) {

            if (item.getPatternReceipt() != null) {
                res.setReceiptName(item.getPatternReceipt().getName());
                res.setReceiptType(item.getPatternReceipt().getType().name());
            }

        } else {

            if (item.getPattern() != null) {
                res.setPatternNumber(item.getPattern().getPatternNumber());
                res.setPatternName(item.getPattern().getName());
            }
        }
    }
}