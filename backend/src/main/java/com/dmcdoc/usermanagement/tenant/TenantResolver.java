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

        // 🚨 Super-admin bypass total
        if (TenantContext.isBypassEnabled()) {
            return null;
        }

        // 1️⃣ JWT
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            UUID tenantId = jwtService.extractTenantId(auth.substring(7));
            if (tenantId != null) {
                return tenantId;
            }
        }

        // 2️⃣ Header explicite
        if (properties.isAllowHeader()) {
            String header = request.getHeader("X-Tenant-Id");
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

        // 3️⃣ Subdomain
        if (properties.isAllowSubdomain()) {
            String host = request.getServerName();
            if (host != null && host.contains(".")) {
                return UUID.nameUUIDFromBytes(
                        host.split("\\.")[0].getBytes());
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
