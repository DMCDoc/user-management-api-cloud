package com.dmcdoc.usermanagement.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tenant")
public record TenantProperties(
        TenantMode mode,
        boolean allowHeader,
        boolean allowSubdomain) {
}
