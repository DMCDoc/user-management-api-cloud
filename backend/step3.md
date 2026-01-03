✅ ÉTAPE 3 — PROTECTION MÉTIER & MULTI-TENANT

Statut : COMPLÈTE

Ce que l’étape 3 devait garantir

✔ Protection centrale des règles métier
✔ Aucune fuite d’information inter-tenant
✔ Exceptions métier correctement levées
✔ Mapping HTTP cohérent côté API
✔ Tests d’intégration représentatifs

Ce qui est effectivement en place
1️⃣ Protection métier au niveau service
assertNotSystemRole(role)


règle centrale

impossible à bypasser via controller

cohérent avec Clean Architecture

✅ OK

2️⃣ Exception explicite pour règle critique
SystemRoleModificationException


exception métier dédiée

pas un RuntimeException générique

exprimée dans le handler

✅ OK

3️⃣ GlobalExceptionHandler aligné sécurité

UUID invalide → 403

Accès interdit → 403

Règle métier violée → 403

Ressource inexistante hors scope → 403

Ressource inexistante autorisée → 404

✅ OK

4️⃣ Tests clés validés

✔ RestaurantControllerIT
✔ RoleRestProtectionIT

Ces deux tests couvrent :

multi-tenant

sécurité

règles métier

mapping HTTP

👉 Si ces deux-là passent, l’architecture est saine.

🧠 Conclusion Étape 3

Les règles métier sont centralisées,
la sécurité est cohérente,
le contrat API est stable.