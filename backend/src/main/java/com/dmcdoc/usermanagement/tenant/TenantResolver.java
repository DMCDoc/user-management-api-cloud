package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.config.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantResolver {

    private final JwtService jwtService;
    private final TenantProperties properties;

    public UUID resolve(HttpServletRequest request) {

        // 1️⃣ JWT
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            UUID tenantId = jwtService.extractTenantId(auth.substring(7));
            if (tenantId != null)
                return tenantId;
        }

        // 2️⃣ Header explicite
        if (properties.isAllowHeader()) {
            String header = request.getHeader("X-Tenant-Id");
            if (header != null)
                return UUID.fromString(header);
        }

        // 3️⃣ Subdomain (optionnel)
        if (properties.isAllowSubdomain()) {
            String host = request.getServerName();
            if (host.contains(".")) {
                return UUID.nameUUIDFromBytes(host.split("\\.")[0].getBytes());
            }
        }

        if (properties.getMode() == TenantMode.STRICT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant resolution failed");
        }

        return null;
    }
}
