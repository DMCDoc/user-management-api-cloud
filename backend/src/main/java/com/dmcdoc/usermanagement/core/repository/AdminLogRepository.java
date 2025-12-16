package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminLogRepository extends JpaRepository<AdminLog, UUID> {
}
/*123 */