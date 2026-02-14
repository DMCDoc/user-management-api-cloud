🚀 Étape 5.4 — Sécurisation & exposition propre de l’Admin API

Objectif :
👉 Finaliser une API Admin propre, sécurisée, cohérente, prête pour tests + frontend.

5.4.1 — Sécurisation des endpoints Admin
🎯 Règle

Seuls les rôles :

ROLE_ADMIN

ROLE_TENANT_ADMIN

doivent accéder aux endpoints /api/admin/**.

📄 SecurityConfig (extrait)
http
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/admin/**")
            .hasAnyRole("ADMIN", "TENANT_ADMIN")
        .anyRequest().authenticated()
    );


✔️ Rien d’autre à modifier ici.