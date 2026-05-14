package com.kalibyte.foundry.order.mapper;

import com.kalibyte.foundry.order.dto.response.OrderItemResponse;
import com.kalibyte.foundry.order.dto.response.OrderResponse;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderMapper {

    // =========================================================
    //  ORDER -> ORDER RESPONSE
    // =========================================================

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "quotationId", source = "quotation.id")
    @Mapping(target = "quotationNumber", source = "quotation.quotationNumber")
    @Mapping(target = "items", source = "items", qualifiedByName = "toItemResponse")
    @Mapping(target = "paymentTermsDisplay", expression = "java(order.getPaymentTermsDisplay())")
    @Mapping(target = "totalQuantity", expression = "java(calculateTotalQuantity(order))")
    @Mapping(target = "producedQuantity", expression = "java(calculateProducedQuantity(order))")
    @Mapping(target = "dispatchedQuantity", expression = "java(calculateDispatchedQuantity(order))")
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);

    // =========================================================
    //  ORDER ITEM -> ORDER ITEM RESPONSE (within an order context)
    // =========================================================

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "metalType", expression = "java(item.getMetalType() != null ? item.getMetalType().getDisplayName() : null)")
    @Mapping(target = "metalCategory", expression = "java(item.getMetalCategory() != null ? item.getMetalCategory().getDisplayName() : null)")
    @Mapping(target = "castingProcessId", source = "castingProcess.id")
    @Mapping(target = "castingProcessName", source = "castingProcess.name")
    @Mapping(target = "isMachiningRequired", source = "isMachiningRequired")
    @Mapping(target = "patternNumber", source = "pattern.patternNumber")
    @Mapping(target = "patternName", source = "pattern.patternName")
    @Mapping(target = "receiptName", source = "patternReceipt.name")
    @Mapping(target = "receiptType", source = "patternReceipt.type")
    @Mapping(target = "receiptMaterial", source = "patternReceipt.material")
    @Mapping(target = "pendingQuantity", expression = "java(item.getPendingQuantity())")
    @Named("toItemResponse")
    OrderItemResponse toItemResponse(OrderItem item);

    @IterableMapping(qualifiedByName = "toItemResponse")
    List<OrderItemResponse> toItemResponseList(List<OrderItem> items);

    // =========================================================
    //  ORDER ITEM -> ORDER ITEM RESPONSE (with order details)
    // =========================================================

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "orderStatus", expression = "java(item.getOrder() != null && item.getOrder().getStatus() != null ? item.getOrder().getStatus().name() : null)")
    @Mapping(target = "customerId", source = "order.customer.id")
    @Mapping(target = "customerName", source = "order.customer.name")
    @Mapping(target = "metalType", expression = "java(item.getMetalType() != null ? item.getMetalType().getDisplayName() : null)")
    @Mapping(target = "metalCategory", expression = "java(item.getMetalCategory() != null ? item.getMetalCategory().getDisplayName() : null)")
    @Mapping(target = "castingProcessId", source = "castingProcess.id")
    @Mapping(target = "castingProcessName", source = "castingProcess.name")
    @Mapping(target = "isMachiningRequired", source = "isMachiningRequired")
    @Mapping(target = "patternNumber", source = "pattern.patternNumber")
    @Mapping(target = "patternName", source = "pattern.patternName")
    @Mapping(target = "receiptName", source = "patternReceipt.name")
    @Mapping(target = "receiptType", source = "patternReceipt.type")
    @Mapping(target = "receiptMaterial", source = "patternReceipt.material")
    @Mapping(target = "pendingQuantity", expression = "java(item.getPendingQuantity())")
    @Named("toItemResponseWithOrder")
    OrderItemResponse toItemResponseWithOrder(OrderItem item);

    @IterableMapping(qualifiedByName = "toItemResponseWithOrder")
    List<OrderItemResponse> toItemResponseWithOrderList(List<OrderItem> items);

    // =========================================================
    //  HELPER METHODS
    // =========================================================

    default Integer calculateTotalQuantity(Order order) {
        if (order == null || order.getItems() == null) return 0;
        return order.getItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    default Integer calculateProducedQuantity(Order order) {
        if (order == null || order.getItems() == null) return 0;
        return order.getItems().stream()
                .mapToInt(i -> i.getProducedQuantity() != null ? i.getProducedQuantity() : 0)
                .sum();
    }

    default Integer calculateDispatchedQuantity(Order order) {
        if (order == null || order.getItems() == null) return 0;
        return order.getItems().stream()
                .mapToInt(i -> i.getDispatchedQuantity() != null ? i.getDispatchedQuantity() : 0)
                .sum();
    }
}