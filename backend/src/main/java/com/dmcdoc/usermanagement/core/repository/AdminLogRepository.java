package com.dmcdoc.usermanagement.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dmcdoc.usermanagement.core.model.AdminLog;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
}
