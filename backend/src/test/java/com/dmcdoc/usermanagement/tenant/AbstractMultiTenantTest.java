package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.config.security.TestJwtBuilder;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc

public abstract class AbstractMultiTenantTest {

    @Autowired
    protected JwtService jwtService;

    // On récupère le secret de test pour le passer au builder de "failles"
    @Value("${security.jwt.secret}")
    private String secret;

    protected final UUID tenantA = UUID.randomUUID();
    protected final UUID tenantB = UUID.randomUUID();

    /**
     * STRATÉGIE A : Production (Via JwtService)
     */
    protected String tokenForTenant(UUID tenantId, String roleName) {
        User user = new User();
        user.setUsername("user-" + tenantId);
        user.setTenantId(tenantId);

        Role role = new Role();
        role.setName(roleName);
        role.setActive(true);
        user.setRoles(Set.of(role));

        return jwtService.generateToken(user);
    }

    protected String tenantAdminToken() {
        return tokenForTenant(tenantA, "ROLE_TENANT_ADMIN");
    }

    protected String superAdminToken() {
        return tokenForTenant(null, "ROLE_SUPER_ADMIN");
    }

    protected String superAdminTokenWithoutTenant() {
        return tokenForTenant(null, "ROLE_SUPER_ADMIN");
    }

    /**
     * STRATÉGIE B : Bas niveau (Via TestJwtBuilder)
     * On passe le secret directement pour que le builder utilise JJWT comme la prod
     */
    protected TestJwtBuilder jwtBuilder() {
        return new TestJwtBuilder(secret);
    }

    protected abstract UUID createEntityForTenant(UUID tenantId);

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

}