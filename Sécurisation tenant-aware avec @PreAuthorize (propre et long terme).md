Sécurisation tenant-aware avec @PreAuthorize (propre et long terme).
Objectif : bloquer toute action cross-tenant, même si un ID valide est fourni.

🎯 Ce que 5.2.4 garantit

Un utilisateur ne peut agir que dans SON tenant

Les contrôleurs restent agnostiques

La règle est centralisée, testable et réutilisable

Compatible JWT / OAuth2 / RBAC