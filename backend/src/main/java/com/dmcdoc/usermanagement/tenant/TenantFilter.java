package com.dmcdoc.usermanagement.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class TenantFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        try {
            HttpServletRequest http = (HttpServletRequest) request;
            String header = http.getHeader("X-Tenant-ID");

            if (header != null) {
                TenantContext.setTenantId(UUID.fromString(header));
            }

            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
