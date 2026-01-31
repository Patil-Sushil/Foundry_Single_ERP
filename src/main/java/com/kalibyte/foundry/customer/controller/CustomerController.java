package com.kalibyte.foundry.customer.controller;

import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.customer.dto.CustomerRequest;
import com.kalibyte.foundry.customer.dto.CustomerResponse;
import com.kalibyte.foundry.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/masterdata/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SALES')")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Customer created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION', 'SALES', 'STORE', 'INVENTORY')")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort) {
        Page<CustomerResponse> customers = customerService.listCustomers(page, size, sort);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customers fetched successfully", customers));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION', 'SALES', 'STORE', 'INVENTORY')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable UUID customerId) {
        CustomerResponse response = customerService.getCustomer(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer fetched successfully", response));
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID customerId,
            @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer updated successfully", response));
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable UUID customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer deleted successfully", null));
    }

    @GetMapping("/{phone}")
    @PreAuthorize("hasAuthority('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<Optional<CustomerResponse>>> getCustomer(@PathVariable String phone) {
        Optional<CustomerResponse> customerResponse = customerService.findByPhone(phone);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer fetched successfully", customerResponse));
    }
}
