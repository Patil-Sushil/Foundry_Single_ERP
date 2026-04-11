package com.kalibyte.foundry.customer.service;

import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.customer.dto.CustomerRequest;
import com.kalibyte.foundry.customer.dto.CustomerResponse;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerRequest request);
    CustomerResponse getCustomerById(UUID customerId);
    PageResponse<CustomerResponse> listCustomers(int page, int size, String sort);
    CustomerResponse updateCustomer(UUID customerId, CustomerRequest request);
    void deleteCustomer(UUID customerId);
    boolean existsByEmail(String email);
    Optional<CustomerResponse> findByPhone(String phone);
}
