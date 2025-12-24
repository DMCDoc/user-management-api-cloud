package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.config.security.JwtPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantSecurityProvider implements TenantCurrentProvider {

    private JwtPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof JwtPrincipal p)) {
            throw new IllegalStateException("Authentication not resolved");
        }
        return p;
    }

    @Override
    public UUID getTenantId() {
        JwtPrincipal principal = principal();

        if (principal.isSuperAdmin()) {
            return null; // 🔥 pas de restriction tenant
        }

        if (principal.getTenantId() == null) {
            throw new IllegalStateException("User not assigned to tenant");
        }

        return principal.getTenantId();
    }

    @Override
    public boolean isSuperAdmin() {
        return principal().isSuperAdmin();
    }
}
