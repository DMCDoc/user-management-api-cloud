Étape 5 — Isolation et sécurité multi-tenant au niveau Repository / JPA

Objectif : garantir zéro fuite de données entre tenants, même en cas d’erreur au niveau service ou controller.

🎯 Objectifs de l’étape 5

Forcer le filtrage par tenant au niveau JPA

Empêcher toute requête « cross-tenant » involontaire

Centraliser la logique → pas de tenantId oublié dans les WHERE

Rendre les tests multi-tenant impossibles à contourner

🧱 Principe retenu (PRO / recommandé)

👉 Hibernate Filter + TenantContext

Pourquoi ?

Transparent pour les repositories

Aucun impact sur les signatures de méthodes

Compatible tests + prod

Standard en environnement multi-tenant JPA