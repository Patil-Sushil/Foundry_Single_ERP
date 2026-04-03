package com.kalibyte.foundry.dashboard.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DashboardException extends RuntimeException {
    private final HttpStatus status;

    public DashboardException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public DashboardException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
