Filtrage Hibernate global par tenant_id (verrou final)

Objectif : forcer automatiquement le tenant_id au niveau SQL, sans dépendre des repositories ni des services.
Résultat : aucune fuite inter-tenant possible, même en cas d’erreur humaine.