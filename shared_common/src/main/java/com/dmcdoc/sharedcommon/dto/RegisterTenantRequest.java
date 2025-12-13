package com.dmcdoc.sharedcommon.dto;

import lombok.Data;

@Data
public class RegisterTenantRequest {
    private String tenantName;
    private String tenantKey;
    private String restaurantName;
    private String restaurantAddress;
    private String metadata;
    private String adminEmail;
    private String password;
    private String firstName;
    private String lastName;
}
