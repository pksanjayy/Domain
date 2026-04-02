import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, exhaustMap, map, tap } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../auth.service';
import { NotificationService } from '../../services/notification.service';
import * as AuthActions from './auth.actions';

@Injectable()
export class AuthEffects {
  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.login),
      exhaustMap(({ request }) =>
        this.authService.login(request).pipe(
          map((response) => {
            this.authService.storeTokens(response.data);
            return AuthActions.loginSuccess({
              user: response.data.user,
              accessToken: response.data.accessToken,
            });
          }),
          catchError((error) => {
            const message =
              error?.error?.message || 'Login failed. Please try again.';
            return of(AuthActions.loginFailure({ error: message }));
          })
        )
      )
    )
  );

  loginSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.loginSuccess),
        tap(() => {
          this.router.navigate(['/dashboard']);
        })
      ),
    { dispatch: false }
  );

  loginFailure$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.loginFailure),
        tap(({ error }) => {
          this.snackBar.open(error, 'Close', {
            duration: 5000,
            panelClass: ['error-snackbar'],
          });
        })
      ),
    { dispatch: false }
  );

  logout$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.logout),
        tap(() => {
          this.authService.logout();
          this.router.navigate(['/login']);
        })
      ),
    { dispatch: false }
  );

  refreshToken$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.refreshToken),
      exhaustMap(() => {
        const refreshToken = this.authService.getRefreshToken();
        if (!refreshToken) {
          return of(AuthActions.refreshTokenFailure({ isNetworkError: false }));
        }
        return this.authService.refreshToken(refreshToken).pipe(
          map((response) => {
            this.authService.storeTokens(response.data);
            return AuthActions.refreshTokenSuccess({
              user: response.data.user,
              accessToken: response.data.accessToken,
            });
          }),
          catchError((err) => {
            const isNetworkError = err.status === 0 || err.status >= 500;
            return of(AuthActions.refreshTokenFailure({ isNetworkError }));
          })
        );
      })
    )
  );

  refreshTokenFailure$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.refreshTokenFailure),
        tap(({ isNetworkError }) => {
          if (!isNetworkError) {
            this.authService.logout();
            // Only navigate to login if not already on the login page
            if (!this.router.url.startsWith('/login')) {
              this.router.navigate(['/login']);
            }
          }
        })
      ),
    { dispatch: false }
  );

  refreshTokenSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.refreshTokenSuccess),
        tap(() => {
          this.notificationService.connect();
        })
      ),
    { dispatch: false }
  );

  constructor(
    private actions$: Actions,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private notificationService: NotificationService
  ) {}
}
