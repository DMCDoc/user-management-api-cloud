▶️ Étape 5 — Rendre les tests tenant-aware (obligatoire)

Objectif :
👉 Faire passer proprement les tests dans le nouveau monde multi-tenant

🧱 Étape 5.1 — Base de test commune multi-tenant

Créer une classe abstraite de base pour tous les tests IT.

AbstractMultiTenantIT

Responsabilités :

définir un tenant de test

initialiser / nettoyer TenantContext

garantir isolation totale

public abstract class AbstractMultiTenantIT {

    protected static final UUID TEST_TENANT =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUpTenant() {
        TenantContext.setTenantId(TEST_TENANT);
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }
}


👉 Tous les tests IT héritent de cette classe

🧪 Étape 5.2 — Tests Repository

Problème :

Les repositories sont filtrés par tenant

Solution :

Le tenant DOIT être présent avant tout save() ou find()

Exemple :

class UserRepositoryIT extends AbstractMultiTenantIT {


👉 Rien d’autre à faire
👉 Le filtre Hibernate fera le reste

🔐 Étape 5.3 — Tests REST / Security

Dans les tests REST :

le tenant doit être dans le JWT

ou dans X-Tenant-ID

Approche recommandée

JWT avec claim tenantId

cohérent avec la prod

👉 On adaptera JwtTestUtils

⚠️ Étape 5.4 — Cas SYSTEM / Bootstrap

Certains tests doivent bypasser le tenant :

@BeforeEach
void setup() {
    TenantContext.enableBypass();
}


Exemples :

création des rôles système

bootstrap admin

migrations

🧼 Étape 5.5 — Nettoyage des anciens tests

À faire :

❌ supprimer les tests mono-tenant obsolètes

✅ renommer clairement :

RestaurantControllerIT

RestaurantMultiTenantIT

Résultat attendu en fin d’étape 5
Élément	État
Tests repository	✅
Tests REST	✅
Tests sécurité	✅
Isolation tenant	🔒 garantie
Design	💎 pro