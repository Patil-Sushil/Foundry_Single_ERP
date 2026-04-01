package com.kalibyte.foundry.customer.exception;

import com.kalibyte.foundry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCustomerException extends BusinessException {
    public InvalidCustomerException(String message) {
        super(message);
    }
}
