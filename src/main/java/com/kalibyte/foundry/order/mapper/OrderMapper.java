package com.kalibyte.foundry.order.mapper;

import com.kalibyte.foundry.order.dto.response.CustomerSummary;
import com.kalibyte.foundry.order.dto.response.OrderItemResponse;
import com.kalibyte.foundry.order.dto.response.OrderResponse;
import com.kalibyte.foundry.order.dto.response.QuotationSummary;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {

        if (order == null) {
            return null;
        }

        List<OrderItemResponse> items = order.getOrderItems() == null
                ? Collections.emptyList()
                : order.getOrderItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .deliveryDate(order.getDeliveryDate())

                .customer(
                        order.getCustomer() != null
                                ? CustomerSummary.builder()
                                .id(order.getCustomer().getId())
                                .name(order.getCustomer().getName())
                                .email(order.getCustomer().getEmail())
                                .phone(order.getCustomer().getPhone())
                                .address(order.getCustomer().getAddress())
                                .build()
                                : null
                )

                .quotation(
                        order.getQuotation() != null
                                ? QuotationSummary.builder()
                                .id(order.getQuotation().getId())
                                .quotationNumber(order.getQuotation().getQuotationNumber())
                                .quotationDate(order.getQuotation().getQuotationDate())
                                .totalAmount(order.getQuotation().getTotalAmount())
                                .build()
                                : null
                )

                .items(items)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .createdBy(order.getCreatedBy())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {

        if (item == null) {
            return null;
        }

        return OrderItemResponse.builder()
                .id(item.getId())
                .productName(item.getProductName())
                .metalType(item.getMetalType())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}