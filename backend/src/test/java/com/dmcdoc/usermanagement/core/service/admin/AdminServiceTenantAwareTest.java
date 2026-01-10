package com.dmcdoc.usermanagement.core.service.admin;

import com.dmcdoc.usermanagement.api.exceptions.ResourceNotFoundException;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTenantAwareTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(userRepository, roleRepository, passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getUserById_returns_user_when_tenant_matches() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User u = new User();
        u.setId(userId);
        u.setTenantId(tenantId);

        when(userRepository.findByIdAndTenantId(eq(userId), eq(tenantId))).thenReturn(Optional.of(u));

        TenantContext.setTenantId(tenantId);

        User result = adminService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository).findByIdAndTenantId(eq(userId), eq(tenantId));
    }

    @Test
    void getUserById_throws_when_not_found_or_wrong_tenant() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.empty());

        TenantContext.setTenantId(tenantId);

        assertThrows(ResourceNotFoundException.class, () -> adminService.getUserById(userId));
        verify(userRepository).findByIdAndTenantId(eq(userId), eq(tenantId));
    }
}
