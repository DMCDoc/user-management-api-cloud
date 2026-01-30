package com.dmcdoc.usermanagement.tenant.exception;

public class MissingTenantException extends RuntimeException {

    public MissingTenantException() {
        super("Tenant identifier is required");
    }
}
