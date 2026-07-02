package com.spkmainan.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for exceptions that map to a specific HTTP status. Throw a
 * subclass from services; {@link GlobalExceptionHandler} renders the response.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
