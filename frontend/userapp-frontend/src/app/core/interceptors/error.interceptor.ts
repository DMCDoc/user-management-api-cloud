import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = '❌ Une erreur est survenue';

      if (error.error instanceof ErrorEvent) {
        errorMessage = `Erreur client: ${error.error.message}`;
      } else {
        switch (error.status) {
          case 0:
            errorMessage = '🚫 Impossible de contacter le serveur';
            break;
          case 400:
            errorMessage = error.error.message || '⚠️ Requête invalide';
            break;
          case 401:
            errorMessage = '🔑 Non autorisé (token manquant ou invalide)';
            break;
          case 403:
            errorMessage = '⛔ Accès refusé';
            break;
          case 404:
            errorMessage = '❓ Ressource introuvable';
            break;
          case 500:
            errorMessage = '💥 Erreur interne du serveur';
            break;
        }
      }

      console.error('📡 HTTP Error:', errorMessage, error);
      return throwError(() => new Error(errorMessage));
    })
  );
};
