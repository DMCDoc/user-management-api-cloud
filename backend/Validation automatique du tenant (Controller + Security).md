5️⃣.2️⃣.3 — Validation automatique du tenant (Controller + Security)
🎯 Objectif EXACT

Le tenant_id ne doit JAMAIS venir du body

Le tenant_id ne doit JAMAIS être libre côté client

Toute requête est automatiquement scellée au tenant courant

Aucune dépendance directe aux repositories dans les controllers

👉 Le tenant est :

soit résolu par le token

soit résolu par le header

puis injecté implicitement dans le service

🧠 Principe d’architecture (important)
HTTP Request
   ↓
TenantResolution (Filter)
   ↓
SecurityContext (User authentifié)
   ↓
Controller (NE CONNAÎT PAS le tenantId)
   ↓
Service (tenantId injecté automatiquement)
   ↓
Repository (tenant-safe)

5️⃣ Compatible avec DB schema / DB par tenant ?

✔️ OUI
Quand tu passeras à :

schema par tenant → CurrentTenantProvider alimente le resolver

DB par tenant → DataSource routing

👉 AUCUN controller à modifier

✅ Résumé 5.2.3

✔️ Tenant invisible côté API
✔️ Impossible à falsifier
✔️ Stateless
✔️ Clean
✔️ Testable
✔️ Évolutif