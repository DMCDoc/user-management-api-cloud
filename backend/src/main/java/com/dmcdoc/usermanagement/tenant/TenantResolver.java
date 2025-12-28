package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.config.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantResolver {

    private final JwtService jwtService;
    private final TenantProperties properties;

    public UUID resolve(HttpServletRequest request) {

        // SUPER ADMIN → bypass
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
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Invalid tenant id");
                }
            }
        }

        if (properties.getMode() == TenantMode.STRICT) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tenant resolution failed");
        }

        return null;
    }
}
