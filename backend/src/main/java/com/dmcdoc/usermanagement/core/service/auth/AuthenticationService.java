package com.dmcdoc.usermanagement.core.service.auth;

import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.sharedcommon.dto.LoginRequest;
import com.dmcdoc.sharedcommon.dto.RefreshRequest;

import java.util.UUID;

public interface AuthenticationService {

    AuthResponse login(LoginRequest request, UUID tenantId);

    AuthResponse refresh(RefreshRequest request, UUID tenantId);
}

/*
 * ✔ Tenant explicite
 * ✔ Aucun détail technique exposé
 * ✔ Compatible REST / OAuth / MagicLink
 * ✔ Testable
 * ✔ Stable dans le temps
 */