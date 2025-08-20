import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, RegisterRequest, AuthResponse } from '../../core';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  registerForm: FormGroup;
  errorMessage: string | null = null;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.registerForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      fullName: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      const payload = this.registerForm.value as RegisterRequest;
      this.authService.register(payload).subscribe({
        next: (res: AuthResponse) => {
          console.log('✅ Register OK:', res);
          this.errorMessage = null;
          this.router.navigateByUrl('/dashboard');
        },
        error: (err) => {
          console.error('❌ Register failed:', err);
          this.errorMessage = err.error?.message || 'Erreur d’inscription';
        }
      });
    }
  }
}
