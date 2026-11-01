import { Component, inject, OnInit } from '@angular/core';
import { LocalAuth } from './services/local-auth';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { PageEditorComponent } from './components/page-editor/page-editor.component';
import { StateService } from './services/state.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, PageEditorComponent],
  template: `
    @if (auth.ready()) {
      <div class="local-account">
        <span>Gestor de Notas · {{ auth.username() }}</span>
        <button type="button" [disabled]="auth.loading()" (click)="auth.logout()">Cerrar sesión</button>
        @if (auth.message()) { <p role="alert">{{ auth.message() }}</p> }
      </div>
      <main class="app-shell">
        <button *ngIf="state.sidebarOpen()" class="sidebar-backdrop"
          aria-label="Cerrar navegación" (click)="state.sidebarOpen.set(false)"></button>
        <app-sidebar [class.sidebar-visible]="state.sidebarOpen()"></app-sidebar>
        <app-page-editor></app-page-editor>
      </main>
    } @else {
      <main class="local-access">
        <aside class="login-brand" aria-hidden="true">
          <div class="login-brand-logo">
            <span class="brand-glyph">
              <svg viewBox="0 0 36 36" fill="none"><rect x="7" y="5" width="22" height="26" rx="5"/><path d="M12 13h12M12 19h12M12 25h8"/><path d="M18 5v26"/></svg>
            </span>
            <span class="brand-name">Gestor de Notas<small>Tu espacio personal</small></span>
          </div>
          <div class="login-brand-copy">
            <h2>Tus ideas,<br>siempre a mano.</h2>
            <p>Notas, proyectos e inspiración en un espacio organizado y privado.</p>
          </div>
          <ul class="login-brand-points">
            <li><span class="brand-point-icon" aria-hidden="true">✓</span> Tus notas en un solo lugar</li>
            <li><span class="brand-point-icon" aria-hidden="true">✓</span> Un espacio para cada idea</li>
            <li><span class="brand-point-icon" aria-hidden="true">✓</span> Acceso personal y protegido</li>
          </ul>
        </aside>
        <section class="login-form-panel" aria-labelledby="access-title">
          <form class="login-card" (ngSubmit)="signIn()" novalidate>
            <div class="login-mobile-brand">
              <span class="brand-glyph" aria-hidden="true">
                <svg viewBox="0 0 36 36" fill="none"><rect x="7" y="5" width="22" height="26" rx="5"/><path d="M12 13h12M12 19h12M12 25h8"/><path d="M18 5v26"/></svg>
              </span>
              <span>Gestor de Notas</span>
            </div>
            <h1 id="access-title">Iniciar sesión</h1>
            <p class="login-lead">Acceso personal a Gestor de Notas.</p>
            @if (auth.message()) {
              <p class="login-message error" role="alert">{{ auth.message() }}</p>
            }
            @if (auth.loading()) {
              <p class="login-message info" role="status">Comprobando tu sesión…</p>
            }
            <label for="username">Usuario</label>
            <div class="login-field">
              <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4 20c1.5-4 4.5-6 8-6s6.5 2 8 6"/></svg>
              <input id="username" name="username" type="text" autocomplete="username" required autofocus
                [(ngModel)]="username" [disabled]="auth.loading()">
            </div>
            <label for="password">Contraseña</label>
            <div class="login-field">
              <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="10" width="14" height="10" rx="2.5"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>
              <input id="password" name="password" autocomplete="current-password" required
                [type]="showPassword ? 'text' : 'password'" [(ngModel)]="password" [disabled]="auth.loading()">
              <button type="button" class="login-reveal" (click)="showPassword = !showPassword"
                [attr.aria-label]="showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'"
                [attr.aria-pressed]="showPassword" [disabled]="auth.loading()">
                @if (showPassword) {
                  <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 3l18 18M10.6 6.1A9.9 9.9 0 0 1 12 6c5 0 8.5 4 9.5 6-.4.9-1.3 2.6-2.6 3.4M6.5 7.6C4.6 8.9 3.2 10.7 2.5 12c1 2 4.5 6 9.5 6 1.6 0 3-.4 4.3-1"/><path d="M9.9 9.9a3 3 0 0 0 4.2 4.2"/></svg>
                } @else {
                  <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12C3.5 10 7 6 12 6s8.5 4 9.5 6c-1 2-4.5 6-9.5 6s-8.5-4-9.5-6z"/><circle cx="12" cy="12" r="3"/></svg>
                }
              </button>
            </div>
            <button type="submit" class="login-submit" [disabled]="auth.loading() || !username.trim() || !password">
              {{ auth.loading() ? 'Ingresando…' : 'Ingresar' }}
            </button>
          </form>
        </section>
      </main>
    }
  `
})
export class AppComponent implements OnInit {
  readonly auth = inject(LocalAuth);
  username = '';
  password = '';
  showPassword = false;
  constructor(public state: StateService) {}
  ngOnInit() { this.auth.check(); }
  signIn() {
    if (this.auth.loading() || !this.username.trim() || !this.password) return;
    this.state.clearSelection();
    this.auth.login(this.username, this.password);
    this.password = '';
  }
}
