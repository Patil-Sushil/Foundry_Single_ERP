package com.kalibyte.foundry.customer.exception;

import com.kalibyte.foundry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends BusinessException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
