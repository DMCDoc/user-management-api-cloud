package com.dmcdoc.sharedcommon.dto;

public record TenantOnboardingRequest(
        String tenantKey,
        String tenantName,
        String adminEmail,
        String password) {
}
