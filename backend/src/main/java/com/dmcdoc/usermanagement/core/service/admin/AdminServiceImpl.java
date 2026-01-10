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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    

    @Override
    public Page<AdminUserResponse> searchUsers(String query, Pageable pageable) {
        UUID tenantId = TenantContext.getTenantId();

        Page<User> users =
                userRepository.findByTenantIdAndUsernameContainingIgnoreCaseOrTenantIdAndEmailContainingIgnoreCase(
                        tenantId, query,
                        tenantId, query,
                        pageable
                );

        return users.map(AdminUserMapper::toResponse);
    }

    @Override
    public void blockUser(UUID userId) {
        User user = userRepository.findByIdAndTenantId(userId, TenantContext.getTenantId())
                .orElseThrow();
        user.setLocked(true);
    }

    @Override
    public void unblockUser(UUID userId) {
        User user = userRepository.findByIdAndTenantId(userId, TenantContext.getTenantId())
                .orElseThrow();
        user.setLocked(false);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findByIdAndTenantId(userId, TenantContext.getTenantId())
                .orElseThrow();
        userRepository.delete(user);
    }

    @Override
    public long countUsersForTenant() {
        return userRepository.countByTenantId(TenantContext.getTenantId());
    }
}
