package com.kalibyte.foundry.customer.service;

import com.kalibyte.foundry.customer.dto.CustomerRequest;
import com.kalibyte.foundry.customer.exception.InvalidCustomerException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class CustomerValidator {

    private static final List<String> ALLOWED_PAYMENT_TERMS = Arrays.asList("NET30", "NET60", "COD", "ADVANCE");

    public void validate(CustomerRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new InvalidCustomerException("Customer name is required");
        }
        if (!StringUtils.hasText(request.getEmail())) {
            throw new InvalidCustomerException("Customer email is required");
        }
        
        if (request.getCreditLimit() != null && request.getCreditLimit().compareTo(BigDecimal.ZERO) < 0) {
             throw new InvalidCustomerException("Credit limit must be positive");
        }

        if (StringUtils.hasText(request.getPaymentTerms()) && !ALLOWED_PAYMENT_TERMS.contains(request.getPaymentTerms())) {
            throw new InvalidCustomerException("Invalid payment terms. Allowed: " + ALLOWED_PAYMENT_TERMS);
        }
        
        // Add more validations like GST format (regex) if needed
    }
}
