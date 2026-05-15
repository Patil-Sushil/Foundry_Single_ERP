package com.kalibyte.foundry.customer.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.customer.dto.CustomerRequest;
import com.kalibyte.foundry.customer.dto.CustomerResponse;
import com.kalibyte.foundry.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/masterdata/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Customer created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'SALES', 'STORE', 'INVENTORY')")
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort) {
        PageResponse<CustomerResponse> customers = customerService.listCustomers(page, size, sort);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customers fetched successfully", customers));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'SALES', 'STORE', 'INVENTORY')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable UUID customerId) {
        CustomerResponse response = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer fetched successfully", response));
    }


    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'SALES', 'STORE', 'INVENTORY')")
    public ResponseEntity<ApiResponse<CustomerResponse>> findByPhone(@PathVariable String phone) {
        Optional<CustomerResponse> customerResponse = customerService.findByPhone(phone);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer fetched successfully", customerResponse.orElse(null)));

    }


    @PatchMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID customerId,
            @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer updated successfully", response));
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable UUID customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer deleted successfully", null));
    }
}
