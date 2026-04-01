package com.kalibyte.foundry.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerListResponse {
    private List<CustomerResponse> customers;
    private long totalCount;
    private int pageNumber;
    private int pageSize;
}
