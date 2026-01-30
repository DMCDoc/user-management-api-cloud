package com.dmcdoc.usermanagement.tenant;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implémentation web : le tenant est résolu depuis la requête HTTP
 * (JWT, header, mode strict, etc.).
 */
@Component
@ConditionalOnWebApplication
@RequiredArgsConstructor
public class WebTenantProvider implements CurrentTenantProvider {

    private final HttpServletRequest request;
    private final TenantResolver tenantResolver;

    @Override
    public UUID getCurrentTenant() {
        return tenantResolver.resolve(request);
    }
}
