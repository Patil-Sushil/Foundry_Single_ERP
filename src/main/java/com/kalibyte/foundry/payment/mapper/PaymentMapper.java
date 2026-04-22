package com.kalibyte.foundry.payment.mapper;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.payment.dto.response.PaymentResponse;
import com.kalibyte.foundry.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "invoice.id", target = "invoiceId")
    @Mapping(source = "invoice.invoiceNumber", target = "invoiceNumber")
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);

    PageResponse toPageResponse(Page<Payment> payments);
}