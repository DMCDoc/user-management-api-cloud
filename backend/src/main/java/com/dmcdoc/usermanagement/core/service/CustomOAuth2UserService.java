package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = (String) oAuth2User.getAttributes().get("email");

        if (email == null || email.isEmpty()) {
            log.error("OAuth2 user has no email attribute");
            throw new RuntimeException("Email manquant dans le profil OAuth2");
        }

        // Crée ou récupère l'utilisateur localement
        User user = userService.findOrCreateByEmailOAuth2(email, oAuth2User);
        log.info("OAuth2 login for user {}", email);

        return oAuth2User;
    }
}
