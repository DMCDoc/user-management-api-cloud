package com.dmcdoc.usermanagement.tenant;

import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.config.security.TestJwtBuilder;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Set;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class AbstractMultiTenantTest {

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Value("${security.jwt.secret}")
    private String secret;

    protected final UUID tenantA = UUID.randomUUID();
    protected final UUID tenantB = UUID.randomUUID();

    protected String tokenForTenant(UUID tenantId, String roleName) {
        String username = (tenantId != null) ? "user-" + tenantId : "superadmin";

        // 1. AJOUT : On bypass le tenant pour pouvoir préparer les données sans être
        // bloqué
        TenantContext.enableBypass();

        try {
            User user = (tenantId != null)
                    ? userRepository.findByUsernameAndTenantId(username, tenantId).orElse(null)
                    : userRepository.findByUsername(username).orElse(null);

            if (user == null) {
                User newUser = new User();
                newUser.setUsername(username);
                // 2. AJOUT : Email obligatoire pour satisfaire la contrainte SQL NOT NULL
                newUser.setEmail(username + "@test.com");
                newUser.setPassword(passwordEncoder.encode("password"));
                newUser.setTenantId(tenantId);
                newUser.setActive(true);

                Role role = roleRepository.findByNameAndTenantId(roleName, tenantId)
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setName(roleName);
                            newRole.setTenantId(tenantId);
                            newRole.setActive(true);
                            return roleRepository.save(newRole);
                        });

                newUser.setRoles(Set.of(role));
                user = userRepository.save(newUser);
            }

            return jwtService.generateToken(user);
        } finally {
            // 3. AJOUT : Très important : on coupe le bypass pour que MockMvc teste
            // réellement la sécurité
            TenantContext.disableBypass();
        }
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

    protected MockHttpServletRequestBuilder getWithTenant(String url, String token, UUID tenantId) {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url)
                .header("Authorization", "Bearer " + token);

        if (tenantId != null) {
            builder.header("X-Tenant-ID", tenantId.toString());
        }
        return builder;
    }

    protected MockHttpServletRequestBuilder postWithTenant(String url, String token, UUID tenantId) {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url)
                .header("Authorization", "Bearer " + token);

        if (tenantId != null) {
            builder.header("X-Tenant-ID", tenantId.toString());
        }
        return builder;
    }

    protected TestJwtBuilder jwtBuilder() {
        return new TestJwtBuilder(secret);
    }

    protected abstract UUID createEntityForTenant(UUID tenantId);

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }
}