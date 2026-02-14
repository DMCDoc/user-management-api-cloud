import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <main class="shell">
      <h1>User Management Frontend</h1>
      <p>Angular 16 standalone setup is ready.</p>
    </main>
  `,
  styles: [
    `
      .shell {
        min-height: 100vh;
        display: grid;
        place-content: center;
        gap: 0.75rem;
        text-align: center;
        padding: 2rem;
      }

      h1 {
        margin: 0;
        font-size: clamp(1.75rem, 2.5vw, 2.5rem);
      }

      p {
        margin: 0;
        color: #4b5563;
      }
    `,
  ],
})
export class AppComponent {}
