package com.dmcdoc.usermanagement.config.security;

/* Copyright (c) 2024 DMC Doc */
/* 
package com.dmcdoc.usermanagement.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * @deprecated Use JwtService helpers for token-based checks or inspect
 *             the `Authentication` directly. This class remains temporarily for
 *             backward compatibility and should be removed once callers
 *             migrated.
 */

/*  @Deprecated
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
*/