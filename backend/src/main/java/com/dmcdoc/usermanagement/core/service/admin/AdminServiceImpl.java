
package com.dmcdoc.usermanagement.core.service.admin;

import com.dmcdoc.sharedcommon.dto.AdminUserResponse;
import com.dmcdoc.usermanagement.core.mapper.AdminUserMapper;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public Page<AdminUserResponse> searchUsers(String query, Pageable pageable) {
        UUID tenantId = requireTenant();

        Page<User> users = userRepository
                .findByTenantIdAndUsernameContainingIgnoreCaseOrTenantIdAndEmailContainingIgnoreCase(
                        tenantId, query,
                        tenantId, query,
                        pageable);

        return users.map(AdminUserMapper::toResponse);
    }

    @Override
    public void blockUser(UUID userId) {
        User user = findUserInCurrentTenant(userId);
        user.setLocked(true);
    }

    @Override
    public void unblockUser(UUID userId) {
        User user = findUserInCurrentTenant(userId);
        user.setLocked(false);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = findUserInCurrentTenant(userId);
        userRepository.delete(user);
    }

    @Override
    public long countUsersForTenant() {
        return userRepository.countByTenantId(requireTenant());
    }

    /* ================= Helpers ================= */

    private UUID requireTenant() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("TenantId is required");
        }
        return tenantId;
    }

    private User findUserInCurrentTenant(UUID userId) {
        return userRepository.findByIdAndTenantId(userId, requireTenant())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
/*
 * ✔ Même garde-fou que UserService
 * ✔ Aucun accès cross-tenant possible
 * ✔ Exceptions claires
 * ✔ Zéro dépendance Hibernate
 * ✔ Zéro dette technique
 * ✔ Facile à tester
 * ✔ Compatible avec ton executor figé
 */