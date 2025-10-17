package com.dmcdoc.usermanagement.core.service;

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

    // Minimal repository interface so the type is resolved during compilation.
    public interface MagicLinkTokenRepository {
        void deleteByExpiresAtBefore(Instant instant);
    }
}
