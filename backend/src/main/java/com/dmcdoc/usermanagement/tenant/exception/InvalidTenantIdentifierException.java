package com.dmcdoc.usermanagement.tenant.exception;

public class InvalidTenantIdentifierException extends RuntimeException {

    public InvalidTenantIdentifierException() {
        super("Invalid tenant identifier");
    }
}
