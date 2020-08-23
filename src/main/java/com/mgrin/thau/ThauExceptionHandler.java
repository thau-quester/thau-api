package com.mgrin.thau;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ThauExceptionHandler extends ResponseEntityExceptionHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ThauExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        LOGGER.error("Server Error", ex);
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, status, request);
        APIError error = new APIError(response.getStatusCode(), ex);
        return new ResponseEntity<>(error.getDTO(), headers, error.getStatus());
    }
    
    @ExceptionHandler(value = { APIError.class })
    protected ResponseEntity<Object> handleAPIError(APIError error, WebRequest request) {
        LOGGER.error("API Error", error);
        return new ResponseEntity<>(error.getDTO(), error.getStatus());
    }
}