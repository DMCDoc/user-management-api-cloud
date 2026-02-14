🚀 GO ÉTAPE 4 — CONTRAT D’ERREUR PRO
Objectif de l’étape 4

Rendre l’API :

prévisible pour un frontend

documentable (OpenAPI)

stable dans le temps

Étape 4 — ce qu’on va faire

1️⃣ Normaliser ErrorResponse
2️⃣ Codes d’erreur fonctionnels (errorCode)
3️⃣ Messages client vs message interne
4️⃣ Base propre pour Swagger / OpenAPI
5️⃣ Tests contractuels d’erreurs

👉 Zéro refactor métier. Uniquement de l’API.

Dis-moi simplement :
“go étape 4”

🎯 Objectif de l’étape 4

Garantir que toutes les erreurs HTTP retournées par l’API sont :

prévisibles (frontend / mobile / autres services)

stables dans le temps

documentables

testables contractuellement

📐 Principe directeur

Le status HTTP indique la catégorie
Le errorCode indique la règle fonctionnelle

Le frontend ne dépend jamais du message texte.

🧱 Cible finale du ErrorResponse
{
  "status": 403,
  "error": "FORBIDDEN",
  "errorCode": "SYSTEM_ROLE_IMMUTABLE",
  "message": "System roles are immutable",
  "path": "/api/roles/123"
}

🧩 DÉCOUPAGE DE L’ÉTAPE 4
4.1 — Normalisation de ErrorResponse
Nouveau contrat
public class ErrorResponse {

    private int status;
    private String error;
    private String errorCode;
    private String message;
    private String path;
}


➡ errorCode devient obligatoire

4.2 — Enum central des codes d’erreur
public enum ErrorCode {

    // Sécurité / multi-tenant
    ACCESS_DENIED,
    INVALID_UUID,
    RESOURCE_FORBIDDEN,

    // Rôles
    SYSTEM_ROLE_IMMUTABLE,
    ROLE_NOT_FOUND,

    // Validation
    VALIDATION_ERROR,
    MALFORMED_REQUEST,

    // Conflits
    USER_ALREADY_EXISTS,
    DATABASE_CONSTRAINT,

    // Fallback
    INTERNAL_ERROR
}


➡ Un seul endroit pour tous les codes

4.3 — Exception métier → code fonctionnel

Exemple :

throw new SystemRoleModificationException(
    ErrorCode.SYSTEM_ROLE_IMMUTABLE,
    "System roles are immutable"
);

4.4 — GlobalExceptionHandler PRO

Chaque handler :

fixe le HTTP status

fixe le ErrorCode

fixe un message lisible

Exemple :

@ExceptionHandler(SystemRoleModificationException.class)
public ResponseEntity<ErrorResponse> handleSystemRole(
        SystemRoleModificationException ex,
        HttpServletRequest request) {

    return build(
        HttpStatus.FORBIDDEN,
        ex.getErrorCode(),
        ex.getMessage(),
        request
    );
}

4.5 — Factory unique
ErrorResponseFactory.create(
    status,
    errorCode,
    message,
    path
)


➡ aucune duplication

4.6 — Tests contractuels (nouvelle couche)

Exemples :

403 + SYSTEM_ROLE_IMMUTABLE

403 + INVALID_UUID

404 + ROLE_NOT_FOUND

400 + VALIDATION_ERROR

➡ le frontend peut mocker l’API sans backend

🧪 Tests existants

✅ RestaurantControllerIT → inchangé
✅ RoleRestProtectionIT → inchangé

On ajoute des tests, on ne casse rien.

📌 Ordre exact d’exécution

1️⃣ ErrorCode enum
2️⃣ ErrorResponse enrichi
3️⃣ SystemRoleModificationException enrichie
4️⃣ ErrorResponseFactory
5️⃣ GlobalExceptionHandler final
6️⃣ Tests contractuels