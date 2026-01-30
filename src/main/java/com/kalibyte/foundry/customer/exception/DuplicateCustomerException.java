package com.kalibyte.foundry.customer.exception;

import com.kalibyte.foundry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCustomerException extends BusinessException {
    public DuplicateCustomerException(String message) {
        super(message);
    }
}
