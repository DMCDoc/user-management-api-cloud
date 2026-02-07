package com.dmcdoc.usermanagement.support;

import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.Tenant;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.TenantRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.security.TestSecurityConfig;
import com.dmcdoc.usermanagement.tenant.TenantContext;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest {

        @Autowired
        protected TenantRepository tenantRepository;

        @Autowired
        protected UserRepository userRepository;

        @Autowired
        protected RoleRepository roleRepository;

        @Autowired
        protected PasswordEncoder passwordEncoder;

        protected Tenant createTenant(String name) {
                Tenant tenant = Tenant.builder()
                                .id(UUID.randomUUID())
                                .name(name)
                                .tenantKey(name.toLowerCase().replaceAll("\\s+", "-"))
                                .active(true)
                                .build();

                return tenantRepository.save(tenant);
        }

        protected Role createRole(String name, UUID tenantId) {
                Role role = Role.builder()
                                .name(name)
                                .build();
                role.setTenantId(tenantId);
                return roleRepository.save(role);
        }

        protected User createUser(
                        String email,
                        String rawPassword,
                        UUID tenantId,
                        Set<Role> roles) {
                User user = User.builder()
                                .email(email)
                                .username(email)
                                .password(passwordEncoder.encode(rawPassword))
                                .roles(roles)
                                .enabled(true)
                                .locked(false)
                                .build();

                user.setTenantId(tenantId);
                return userRepository.save(user);
        }

        protected void setTenant(UUID tenantId) {
                TenantContext.setTenantId(tenantId);
        }

        protected void clearTenant() {
                TenantContext.clear();
        }

        public TestEntities createTenantWithUser(
                        String tenantName,
                        String userEmail,
                        String rawPassword,
                        String roleName) {
                Tenant tenant = createTenant(tenantName);

                // IMPORTANT : activer le tenant AVANT création role/user
                setTenant(tenant.getId());

                Role role = createRole(roleName, tenant.getId());
                User user = createUser(userEmail, rawPassword, tenant.getId(), Set.of(role));

                return new TestEntities(tenant, role, user);
        }

        /**
         * Backwards-compatible overload: default password and role.
         */
        public TestEntities createTenantWithUser(String tenantName, String userEmail) {
                return createTenantWithUser(tenantName, userEmail, "password123", "ROLE_USER");
        }

        /**
         * Backwards-compatible overload: control whether the tenant is activated.
         */
        public TestEntities createTenantWithUser(String tenantName, String userEmail, String rawPassword, String roleName, boolean activate) {
                Tenant tenant = createTenant(tenantName);

                if (activate) {
                        // IMPORTANT : activer le tenant AVANT création role/user
                        setTenant(tenant.getId());
                } else {
                        clearTenant();
                }

                Role role = createRole(roleName, tenant.getId());
                User user = createUser(userEmail, rawPassword, tenant.getId(), Set.of(role));

                // Do not leave tenant activated when requested not to
                if (!activate) {
                        clearTenant();
                }

                return new TestEntities(tenant, role, user);
        }

        @AfterEach
        void cleanupTenantContext() {
                TenantContext.clear();
        }

        public static class TestEntities {
                public final Tenant tenant;
                public final Role role;
                public final User user;

                public TestEntities(Tenant tenant, Role role, User user) {
                        this.tenant = tenant;
                        this.role = role;
                        this.user = user;
                }
        }
}
