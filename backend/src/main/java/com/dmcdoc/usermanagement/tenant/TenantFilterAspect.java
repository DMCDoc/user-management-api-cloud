package com.dmcdoc.usermanagement.tenant;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class TenantFilterAspect {

    private final TenantResolver tenantResolver;
    private final HibernateTenantFilterEnabler filterEnabler;
    private final HttpServletRequest request;

    @Before("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public void enableTenantFilter() {
        UUID tenantId = tenantResolver.resolve(request);
        filterEnabler.enableTenantFilter(tenantId);
    }
}
