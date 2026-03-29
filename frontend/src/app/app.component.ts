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
      // No saved token → mark initialization complete so guards unblock
      this.store.dispatch(initComplete());
      return;
    }

    if (this.jwtService.isTokenExpired(token)) {
      // Access token expired → try refreshing
      this.store.dispatch(refreshToken());
      return;
    }

    // Token is valid → rehydrate user from /api/auth/me
    this.authService.getMe().subscribe({
      next: (res) => {
        this.store.dispatch(loadUser({ user: res.data, accessToken: token }));
        this.notificationService.connect();
      },
      error: () => {
        // Token invalid on server side → try refresh
        this.store.dispatch(refreshToken());
      },
    });
  }
}

