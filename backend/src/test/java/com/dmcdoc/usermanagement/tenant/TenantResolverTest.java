package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.config.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TenantResolverTest {

    @Test
    void headerTenantAllowed_returnsUuid() {
        JwtService jwtService = mock(JwtService.class);
        TenantProperties props = new TenantProperties(TenantMode.AUTO_PROVISION, true, false);

        TenantResolver resolver = new TenantResolver(jwtService, props);

        MockHttpServletRequest req = new MockHttpServletRequest();
        UUID expected = UUID.randomUUID();
        req.addHeader("X-Tenant-Id", expected.toString());

        UUID resolved = resolver.resolve(req);

        assertEquals(expected, resolved);
    }

    @Test
    void noTenantInStrict_throws403() {
        JwtService jwtService = mock(JwtService.class);
        TenantProperties props = new TenantProperties(TenantMode.STRICT, false, false);

        TenantResolver resolver = new TenantResolver(jwtService, props);

        MockHttpServletRequest req = new MockHttpServletRequest();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> resolver.resolve(req));
        assertEquals(403, ex.getStatusCode().value());
    }
}
