package com.dmcdoc.usermanagement.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static boolean isSuperAdmin(Authentication auth) {
        if (auth == null)
            return false;

        for (GrantedAuthority a : auth.getAuthorities()) {
            if ("ROLE_SUPER_ADMIN".equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
