import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth/auth.service';
import { Router } from '@angular/router';
import { UserResponse } from '../models/user-response';

@Component({
  standalone: true,
  selector: 'app-dashboard',
  imports: [CommonModule],
  template: `
    <h2>Dashboard</h2>
    <ng-container *ngIf="user; else loading">
      <p>Bienvenue, <strong>{{ user.fullName || user.username }}</strong></p>
      <p>Email: {{ user.email }}</p>
      <button (click)="logout()">Logout</button>
    </ng-container>
    <ng-template #loading>
      <p>Chargement du profil...</p>
    </ng-template>
  `
})
export class DashboardComponent implements OnInit {
  user?: UserResponse;

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.auth.getProfile().subscribe({
      next: u => this.user = u,
      error: err => {
        console.error('Profile error', err);
        this.router.navigate(['/login']);
      }
    });
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
