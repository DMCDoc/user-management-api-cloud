🧭 2. Classification des services (important)

On ne refactorise pas tout pareil.
Il y a 3 catégories.

🟦 Catégorie A — Services CRITIQUES multi-tenant (PRIORITÉ 1)

👉 Ceux qui :

créent des entités tenantées

assignent des rôles

font de la sécurité

À traiter en premier
UserService
UserServiceImpl
RoleService
RoleServiceImpl
TenantService
TenantServiceImpl
RestaurantService
RestaurantServiceImpl


🎯 Règle :

ZÉRO Hibernate

ZÉRO Session

ZÉRO disableFilter

tout passe par :

repositories

HibernateSystemQueryExecutor si nécessaire

✔ UserServiceImpl : déjà en bonne voie
👉 C’est le modèle à suivre

🟨 Catégorie B — Services “transverses” (PRIORITÉ 2)

👉 Ils touchent plusieurs domaines mais pas directement le tenant

AdminService
AuthenticationService
OAuth2Service
CustomOAuth2UserService
RefreshTokenService
PasswordResetService
MagicLinkService


🎯 Règle :

ils consomment UserService / TenantService

ils ne décident jamais du tenant

ils n’ont pas besoin d’HibernateSystemQueryExecutor

⚠️ On les adapte APRÈS avoir figé User / Role / Tenant.

🟩 Catégorie C — Services techniques (PRIORITÉ 3)

👉 Peu ou pas de multi-tenant direct

LogService
MailService
MagicLinkCleanupTask
TenantAutoProvisioningService


🎯 Règle :

pas de refacto urgent

juste s’assurer qu’ils n’accèdent pas aux repos tenantés sans contexte

🗺️ 3. Ordre EXACT recommandé (important)
Phase 1 — verrouillage (maintenant)

Finaliser HibernateSystemQueryExecutor

Valider UserServiceImpl

Tests IT UserService ✔

Phase 2 — cohérence rôles

RoleServiceImpl

Centralisation création / lecture rôles

Tests IT RoleService

Phase 3 — tenants

TenantServiceImpl

TenantAutoProvisioningService

Tests IT tenant

Phase 4 — domaine métier

RestaurantServiceImpl

Tests multi-tenant stricts

Phase 5 — auth & admin

Authentication / OAuth2

AdminService

🧪 4. Règles de test (à ne plus casser)

✔ IT tests = filtre activé manuellement
✔ Rôles système = toujours via SystemTenant
✔ Aucun test ne dépend d’un état caché

👉 Ce que tu viens de corriger dans UserServiceIT est exactement la bonne pratique

✅ Conclusion claire

Ta structure n’est pas cassée

Tu es en train de la professionnaliser

UserServiceImpl devient le service de référence

La roadmap est claire et maîtrisée

👉 Prochaine étape logique :
Phase 1 – finaliser proprement HibernateSystemQueryExecutor (API définitive)
Ensuite on déroule service par service, sans stress.