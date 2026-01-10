package com.dmcdoc.usermanagement.core.service.admin;

import com.dmcdoc.sharedcommon.dto.AdminUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminService {

    Page<AdminUserResponse> searchUsers(String query, Pageable pageable);

    void blockUser(UUID userId);

    void unblockUser(UUID userId);

    void deleteUser(UUID userId);

    long countUsersForTenant();
}
