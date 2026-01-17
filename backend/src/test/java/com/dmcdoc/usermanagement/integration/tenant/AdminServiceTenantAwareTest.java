package com.dmcdoc.usermanagement.integration.tenant;

import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import com.dmcdoc.usermanagement.core.service.admin.AdminServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTenantAwareTest {

    @Mock
    private UserRepository userRepository;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(userRepository);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void blockUser_locks_user_when_tenant_matches() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setLocked(false);

        TenantContext.setTenantId(tenantId);

        when(userRepository.findByIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.of(user));

        adminService.blockUser(userId);

        assertTrue(user.isLocked());
        verify(userRepository).findByIdAndTenantId(userId, tenantId);
    }

    @Test
    void blockUser_throws_when_user_not_in_tenant() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        TenantContext.setTenantId(tenantId);

        when(userRepository.findByIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminService.blockUser(userId));
    }
}
