

package com.dmcdoc.usermanagement.core.service.auth;

import com.dmcdoc.usermanagement.core.model.OAuth2Provider;
import com.dmcdoc.usermanagement.core.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service

@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final @Lazy UserService userService;

    public CustomOAuth2UserService(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);
        String registrationId = request.getClientRegistration().getRegistrationId().toUpperCase();
        OAuth2Provider provider = OAuth2Provider.valueOf(registrationId);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractEmail(attributes, registrationId);

        if (email == null) {
            throw new IllegalArgumentException("Impossible de récupérer l'email pour le provider : " + registrationId);
        }

        log.info("Authentification OAuth2 réussie : provider={} email={}", provider, email);
        userService.findOrCreateByEmailOAuth2(
                email,
                provider,
                com.dmcdoc.usermanagement.tenant.TenantContext.getTenantId());

        return oAuth2User;
    }

    private String extractEmail(Map<String, Object> attributes, String provider) {
        return switch (provider) {
            case "GOOGLE" -> (String) attributes.get("email");
            case "FACEBOOK" -> (String) attributes.get("email");
            case "GITHUB" -> (String) attributes.get("email");
            default -> null;
        };
    }
}
