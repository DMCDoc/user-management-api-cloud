package com.dmcdoc.usermanagement.tenant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TenantExceptionHandler {

    @ExceptionHandler(MissingTenantException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handleMissingTenant() {
    }
}
