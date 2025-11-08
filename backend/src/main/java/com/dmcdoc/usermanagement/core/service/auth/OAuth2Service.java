package com.dmcdoc.usermanagement.core.service.auth;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.dmcdoc.usermanagement.core.model.OAuth2Provider;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.service.UserService;
import com.dmcdoc.usermanagement.config.security.JwtUtils;

@Service
public class OAuth2Service {

    private final UserService userService;
    private final JwtUtils jwtUtils; // remplace JwtService

    public OAuth2Service(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    public String processOAuth2UserAndGetJwt(OAuth2User oAuth2User) {
        String email = extractEmail(oAuth2User);
        if (email == null)
            throw new IllegalArgumentException("Email manquant du fournisseur OAuth2");

        // 🔧 utilise la nouvelle méthode qu’on va ajouter juste après
        User user = userService.findOrCreateByEmailOAuth2(email, OAuth2Provider.GOOGLE);

        // ✅ génère le token via JwtUtils (ton implémentation existante)
        return jwtUtils.generateToken(user);
    }

    private String extractEmail(OAuth2User user) {
        Object mail = user.getAttributes().get("email");
        return mail == null ? null : mail.toString();
    }
}
