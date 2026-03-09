package com.kalibyte.foundry.accounts.mapper;

import com.kalibyte.foundry.accounts.dto.response.PaymentResponse;
import com.kalibyte.foundry.accounts.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "invoice.id", target = "invoiceId")
    PaymentResponse toResponse(Payment payment);
}
