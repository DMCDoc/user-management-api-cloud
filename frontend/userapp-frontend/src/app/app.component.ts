import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `<h1>Mon App Angular</h1>
    <router-outlet></router-outlet>
  `,
  imports: [RouterModule]
})
export class AppComponent {}
