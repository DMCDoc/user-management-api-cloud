import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RegisterRequest } from '../models/register-request';
import { AuthResponse } from '../models/auth-response';
import { UserResponse } from '../models/user-response';
import { Router } from '@angular/router';
import { LoginRequest } from '../models/login-request';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private base = `${environment.apiUrl}/auth`; // ✅ cohérent avec Spring Boot

  constructor(private http: HttpClient, private router: Router) { }

  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/register`, payload)
      .pipe(tap(res => localStorage.setItem('jwt', res.token)));
  }

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/login`, payload)
      .pipe(
        tap(response => {
          localStorage.setItem('jwt', response.token);
        })
      );
  }

  getProfile(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.base}/profile`);
  }

  logout(): void {
    localStorage.removeItem('jwt');
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('jwt');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
