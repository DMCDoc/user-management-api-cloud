Activation automatique et sûre du tenant (Hibernate Filter)

Objectif :
👉 garantir que toutes les requêtes JPA sont implicitement tenant-scopées, sans dépendre des développeurs.

1️⃣ Principe (important)

Hibernate n’active pas les filtres tout seul.

Il faut :

lire le tenant courant (TenantContext)

activer le filtre Hibernate

injecter le paramètre tenantId

le désactiver proprement en fin de requête

➡️ 1 requête HTTP = 1 activation

Ordre des filtres (CRITIQUE)

Dans SecurityConfig :

http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
http.addFilterAfter(hibernateTenantFilter, JwtAuthenticationFilter.class);

Pourquoi ?

JWT → authentification

JWT → résolution tenant (ou onboarding)

Hibernate → filtre tenant actif

Controller / Service / Repository

➡️ Ordre non négociable

4️⃣ Résultat concret
✔️ Sécurité

Impossible d’accéder aux données d’un autre tenant

Même avec un mauvais repository

Même avec un oubli de tenantId

✔️ Maintenance

Plus besoin de findByTenantId() partout

Le filtre protège automatiquement

✔️ Évolutivité

Compatible :

DB unique + tenant_id

DB schema par tenant

DB par tenant (plus tard)

******

Ordre des filtres (CRITIQUE)

Dans SecurityConfig :

http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
http.addFilterAfter(hibernateTenantFilter, JwtAuthenticationFilter.class);

Pourquoi ?

JWT → authentification

JWT → résolution tenant (ou onboarding)

Hibernate → filtre tenant actif

Controller / Service / Repository

➡️ Ordre non négociable

4️⃣ Résultat concret
✔️ Sécurité

Impossible d’accéder aux données d’un autre tenant

Même avec un mauvais repository

Même avec un oubli de tenantId

✔️ Maintenance

Plus besoin de findByTenantId() partout

Le filtre protège automatiquement

✔️ Évolutivité

Compatible :

DB unique + tenant_id

DB schema par tenant

DB par tenant (plus tard)
