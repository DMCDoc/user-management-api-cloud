import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <h2>Login</h2>
    <form (ngSubmit)="onSubmit()">
      <input [(ngModel)]="username" name="username" placeholder="Username" required />
      <input [(ngModel)]="password" name="password" type="password" placeholder="Password" required />
      <button type="submit">Login</button>
    </form>
    <p>Pas de compte ? <a routerLink="/register">Créer un compte</a></p>
  `
})
export class LoginComponent {
  username = '';
  password = '';

  constructor(private auth: AuthService, private router: Router) { }

  onSubmit() {
    const payload = { username: this.username, password: this.password };
    this.auth.login(payload)
      .subscribe({
        next: () => this.router.navigateByUrl('/dashboard'),
        error: err => console.error('Login error', err)
      });
  }

}
