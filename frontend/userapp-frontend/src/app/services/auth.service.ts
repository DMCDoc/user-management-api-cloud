import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private API_URL = 'https://userapp.local'; // ou http://192.168.56.10 si DNS non configuré

  constructor(private http: HttpClient) {}

  login(credentials: { username: string; password: string }): Observable<any> {
    return this.http.post(`${this.API_URL}/users/login`, credentials);
  }

  register(data: { username: string; password: string }): Observable<any> {
    return this.http.post(`${this.API_URL}/users/register`, data);
  }
}
