package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.tenant.exception.InvalidTenantIdentifierException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantResolver {

    private final JwtService jwtService;
    private final TenantProperties properties;

    public UUID resolve(HttpServletRequest request) {

        // Déjà résolu (JWT filter, etc.)
        if (TenantContext.isResolved()) {
            return TenantContext.getTenantId();
        }

        // SUPER ADMIN
        if (TenantContext.isBypassEnabled()) {
            return null;
        }

        // JWT
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            UUID tenantId = jwtService.extractTenantId(auth.substring(7));
            if (tenantId != null) {
                return tenantId;
            }
        }

        // HEADER (optionnel)
        if (properties.isAllowHeader()) {
            String header = request.getHeader("X-Tenant-ID");
            if (header != null) {
                try {
                    return UUID.fromString(header);
                } catch (IllegalArgumentException e) {
                    throw new InvalidTenantIdentifierException();
                }
            }
        }

        if (properties.getMode() == TenantMode.STRICT) {
            throw new InvalidTenantIdentifierException();
        }

        return null;
    }
}
