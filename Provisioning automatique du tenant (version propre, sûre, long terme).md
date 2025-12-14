Provisioning automatique du tenant (version propre, sûre, long terme)
TenantProvisioningService

Objectif :
👉 Lorsqu’un tenant est créé, tout ce qui est structurel est provisionné une seule fois, de façon atomique, sécurisée et évolutive.

On reste volontairement sur tenant_id (pas encore schema/db par tenant).

5.3.1 — Vue d’ensemble (ce qui se passe)
Déclencheur

Onboarding

OAuth2 first login

Création admin manuelle

Pipeline
Create Tenant
   ↓
Create Tenant Admin
   ↓
Provision defaults (roles, data, quotas…)
   ↓
Ready

5.3.2 — Service dédié : TenantProvisioningService

👉 Jamais dans un controller
👉 Jamais dans un filter
👉 Transaction unique

ce service est clé

✔️ Transaction atomique

Tenant sans admin = rollback

✔️ Zéro dépendance web

Testable

Réutilisable

✔️ Évolutif

Ajout futur :

quotas

defaults (restaurants, settings…)

schema/db creation

✔️ tenant_id immutable
✔️ Pas de TenantContext
✔️ Repositories tenant-aware
✔️ Services propres
✔️ OAuth2 séparé