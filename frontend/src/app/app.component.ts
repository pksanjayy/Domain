import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { JwtService } from './core/auth/jwt.service';
import { AuthService } from './core/auth/auth.service';
import { loadUser, refreshToken, initComplete } from './core/auth/store/auth.actions';
import { NotificationService } from './core/services/notification.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  constructor(
    private store: Store,
    private jwtService: JwtService,
    private authService: AuthService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.tryRestoreSession();
  }

  private tryRestoreSession(): void {
    const token = this.jwtService.getToken();
    if (!token) {
      this.store.dispatch(initComplete());
      return;
    }

    if (this.jwtService.isTokenExpired(token)) {
      this.store.dispatch(refreshToken());
      return;
    }

    // ── SYNCHRONOUS restore from JWT payload ──
    // This immediately unblocks the AuthGuard so the user stays on their current page.
    // No HTTP call is needed for this step.
    const payload = this.jwtService.getTokenPayload(token);
    if (!payload || !payload.sub) {
      this.store.dispatch(initComplete());
      return;
    }

    // Build a minimal user from the JWT claims and dispatch IMMEDIATELY (sync).
    // This sets initialized=true + isAuthenticated=true so guards unblock instantly.
    const minimalUser: any = {
      id: payload.userId,
      username: payload.sub,
      role: payload.role,
      menus: [],
      permissions: [],
    };
    this.store.dispatch(loadUser({ user: minimalUser, accessToken: token }));

    // ── ASYNC upgrade: fetch full profile (menus, permissions) in background ──
    this.authService.getMe().subscribe({
      next: (res) => {
        // Upgrade the minimal user with full server profile
        this.store.dispatch(loadUser({ user: res.data, accessToken: token }));
        this.notificationService.connect();
      },
      error: () => {
        // Server unreachable — minimal user from JWT is already loaded, app stays usable.
        // Menus/permissions will be empty but the user won't be kicked to login.
      },
    });
  }
}

