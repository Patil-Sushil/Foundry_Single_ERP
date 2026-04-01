package com.kalibyte.foundry.qa.customerreturn.mapper;

import com.kalibyte.foundry.qa.customerreturn.dto.CustomerReturnRequest;
import com.kalibyte.foundry.qa.customerreturn.dto.CustomerReturnResponse;
import com.kalibyte.foundry.qa.customerreturn.entity.CustomerReturn;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface CustomerReturnMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "order.id", source = "orderId")
    @Mapping(target = "orderItem.id", source = "orderItemId")
    CustomerReturn toEntity(CustomerReturnRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "orderItemId", source = "orderItem.id")
    @Mapping(target = "itemName", source = "orderItem.partName")
    @Mapping(target = "inspectionNumber", source = "inspection.inspectionNumber")
    CustomerReturnResponse toResponse(CustomerReturn entity);

    List<CustomerReturnResponse> toResponseList(List<CustomerReturn> list);
}
