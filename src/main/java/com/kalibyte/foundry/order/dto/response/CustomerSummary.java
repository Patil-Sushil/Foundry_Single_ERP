package com.kalibyte.foundry.order.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class CustomerSummary {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String address;
}