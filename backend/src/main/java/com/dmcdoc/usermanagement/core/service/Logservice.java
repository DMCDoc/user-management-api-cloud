package com.dmcdoc.usermanagement.core.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dmcdoc.usermanagement.core.model.AdminLog;
import com.dmcdoc.usermanagement.core.repository.AdminLogRepository;

/** Service pour enregistrer les actions administratives. */
@Service
class Logservice {

	private final AdminLogRepository adminLogRepository;

	@Autowired
	Logservice(AdminLogRepository adminLogRepository) {
		this.adminLogRepository = adminLogRepository;
	}

	void logService(String action, UUID userId) {
		AdminLog log = new AdminLog();
		// SecurityUtil not available in this module; leave email empty for now
		log.setAdminEmail(null);
		log.setAction(action);
		log.setUserId(userId);
		log.setTimestamp(LocalDateTime.now());
		adminLogRepository.save(log);
	}
}
