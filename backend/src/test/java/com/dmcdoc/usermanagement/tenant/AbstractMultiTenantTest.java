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
        String username = (tenantId != null)
                ? "user-" + tenantId
                : "superadmin";

        TenantContext.enableBypass();

        try {
            User user = (tenantId != null)
                    ? userRepository.findByUsernameAndTenantId(username, tenantId).orElse(null)
                    : userRepository.findByUsername(username).orElse(null);

            if (user == null) {
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setEmail(username + "@test.com");
                newUser.setPassword(passwordEncoder.encode("password"));
                newUser.setActive(true);

                // 🔐 RÈGLE ABSOLUE
                Role role;
                if ("ROLE_SUPER_ADMIN".equals(roleName)) {
                    newUser.setTenantId(SystemTenant.SYSTEM_TENANT);

                    role = roleRepository.findByNameAndTenantId(
                            "ROLE_SUPER_ADMIN",
                            SystemTenant.SYSTEM_TENANT)
                            .orElseThrow(() -> new IllegalStateException("ROLE_SUPER_ADMIN must exist"));
                } else {
                    newUser.setTenantId(tenantId);

                    role = roleRepository.findByNameAndTenantId(roleName, tenantId)
                            .orElseGet(() -> {
                                Role newRole = new Role();
                                newRole.setName(roleName);
                                newRole.setTenantId(tenantId);
                                newRole.setActive(true);
                                return roleRepository.save(newRole);
                            });
                }

                newUser.setRoles(Set.of(role));
                user = userRepository.save(newUser);
            }

            return jwtService.generateToken(user);
        } finally {
            TenantContext.disableBypass();
        }
    }

    protected String tenantAdminToken() {
        return tokenForTenant(tenantA, "ROLE_TENANT_ADMIN");
    }

    protected String superAdminToken() {
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

    protected TestJwtBuilder jwtBuilder() {
        return new TestJwtBuilder(secret);
    }

    protected abstract UUID createEntityForTenant(UUID tenantId);

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }
}
