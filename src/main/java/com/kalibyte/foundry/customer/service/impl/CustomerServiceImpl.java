package com.kalibyte.foundry.customer.service.impl;

import com.kalibyte.foundry.customer.dto.CustomerRequest;
import com.kalibyte.foundry.customer.dto.CustomerResponse;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.exception.CustomerNotFoundException;
import com.kalibyte.foundry.customer.exception.DuplicateCustomerException;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.customer.service.CustomerService;
import com.kalibyte.foundry.customer.service.CustomerValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerValidator validator;
    private final org.modelmapper.ModelMapper modelMapper;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        validator.validate(request);

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateCustomerException("Email already exists");
        }

        if(customerRepository.findByPhone(request.getPhone()).isPresent()){
            throw new DuplicateCustomerException("Phone number already exists");
        }


        Customer customer = modelMapper.map(request, Customer.class);
        if (customer.getCountry() == null) {
            customer.setCountry("India");
        }
        customer.setStatus("ACTIVE");
        Customer saved = customerRepository.save(customer);
        
        return modelMapper.map(saved, CustomerResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        return modelMapper.map(customer, CustomerResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> listCustomers(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        // Repository already scoped to tenant via connection
        return customerRepository.findAll(pageable)
                .map(customer -> modelMapper.map(customer, CustomerResponse.class));
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID customerId, CustomerRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        

        validator.validate(request);
        modelMapper.map(request, customer);
        
        if (customer.getCountry() == null) {
            customer.setCountry("India");
        }
        
        Customer updated = customerRepository.save(customer);
        return modelMapper.map(updated, CustomerResponse.class);
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        customer.setStatus("INACTIVE"); // Soft delete
        customerRepository.save(customer);
    }


    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }



    @Override
    public Optional<CustomerResponse> findByPhone(String phone) {

        return customerRepository.findByPhone(phone)
                .map(customer -> modelMapper.map(customer, CustomerResponse.class));
    }


}
