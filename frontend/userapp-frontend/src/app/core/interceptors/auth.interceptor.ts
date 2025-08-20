import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // 🔑 Récupération du token stocké (localStorage ou sessionStorage)
  const token = localStorage.getItem('token');

  if (token) {
    // 👉 Clone la requête et ajoute l'en-tête Authorization
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(authReq);
  }

  // 👉 Si pas de token, on continue normalement
  return next(req);
};
