package com.kalibyte.foundry.order.mapper;

import com.kalibyte.foundry.order.dto.response.CustomerSummary;
import com.kalibyte.foundry.order.dto.response.OrderItemResponse;
import com.kalibyte.foundry.order.dto.response.OrderResponse;
import com.kalibyte.foundry.order.dto.response.QuotationSummary;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.quotation.entity.Quotation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    //------------------------------------------------
    // ORDER → RESPONSE
    //------------------------------------------------

    @Mapping(source = "orderItems", target = "items")
    @Mapping(source = "customer", target = "customer")
    @Mapping(source = "quotation", target = "quotation")
    @Mapping(source = "placeOfSupply", target = "placeOfSupply")
    @Mapping(source = "poReference", target = "poReference")
    OrderResponse toResponse(Order order);

    //------------------------------------------------
    // ORDER ITEM → RESPONSE
    //------------------------------------------------

    OrderItemResponse toItemResponse(OrderItem item);

    List<OrderItemResponse> toItemResponses(List<OrderItem> items);

    //------------------------------------------------
    // CUSTOMER → SUMMARY
    //------------------------------------------------

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    CustomerSummary toCustomerSummary(Customer customer);

    //------------------------------------------------
    // QUOTATION → SUMMARY
    //------------------------------------------------

    @Mapping(source = "id", target = "id")
    @Mapping(source = "quotationNumber", target = "quotationNumber")
    @Mapping(source = "quotationDate", target = "quotationDate")
    @Mapping(source = "totalAmount", target = "totalAmount")
    QuotationSummary toQuotationSummary(Quotation quotation);
}