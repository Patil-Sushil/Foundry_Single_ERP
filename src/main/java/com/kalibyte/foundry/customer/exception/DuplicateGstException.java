package com.kalibyte.foundry.customer.exception;

import com.kalibyte.foundry.common.exception.BusinessException;

public class DuplicateGstException extends BusinessException {
    public DuplicateGstException(String message) {
        super(message);
    }
}
