package com.dmcdoc.usermanagement.core.repository;

import com.dmcdoc.usermanagement.core.model.MagicLinkToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.Instant;

public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkToken, String> {
    Optional<MagicLinkToken> findByToken(String token);

    void deleteByExpiresAtBefore(Instant instant);
}
