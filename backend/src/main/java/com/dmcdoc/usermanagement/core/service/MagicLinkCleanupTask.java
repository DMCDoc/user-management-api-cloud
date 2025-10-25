package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.repository.MagicLinkTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class MagicLinkCleanupTask {

    private final MagicLinkTokenRepository tokenRepo;

    @Scheduled(cron = "0 0/30 * * * *") // toutes les 30 minutes
    public void cleanup() {
        tokenRepo.deleteByExpiresAtBefore(Instant.now());
    }
}
