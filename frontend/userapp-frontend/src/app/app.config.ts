import { ApplicationConfig, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter, withEnabledBlockingInitialNavigation } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor, errorInterceptor, jwtInterceptor } from './core';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([
        authInterceptor,   // 🔑 ajoute le JWT automatiquement
        errorInterceptor,   // ⚠️ gère les erreurs API
        jwtInterceptor      // 🛡️ ajoute l'en-tête Authorization si le token est présent
      ])
    ),
    provideZonelessChangeDetection(),
    provideRouter(
      [
        // Vos routes ici
      ],
      withEnabledBlockingInitialNavigation()
    )
  ]
};
