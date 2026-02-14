Garantir que l’Admin fonctionne réellement :

en REST

en sécurité

en multi-tenant

avec des tests fiables

🔧 5.3 — Plan technique (ordre strict)

1️⃣ DTO Admin dédiés (sorties propres)

AdminUserResponse

plus de User exposé côté API

2️⃣ Mapper Admin explicite

AdminUserMapper

aucune logique dans le controller

3️⃣ AdminController finalisé

endpoints cohérents

signatures stables

@PreAuthorize strict

4️⃣ Tests d’intégration Admin (multi-tenant)

list users

block / unblock

delete

isolation inter-tenant