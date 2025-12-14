/*
✔️ Stateless
✔️ Thread-safe
✔️ Pas de ThreadLocal
✔️ Compatible DB par tenant */


package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.core.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantSecurityProvider implements TenantCurrentProvider {

    @Override
    public UUID getTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("Tenant cannot be resolved");
        }

        if (user.getTenantId() == null) {
            throw new IllegalStateException("User not assigned to tenant");
        }

        return user.getTenantId();
    }
}
