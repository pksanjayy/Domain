import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Store } from '@ngrx/store';
import { MatSnackBar } from '@angular/material/snack-bar';
import { refreshToken } from '../auth/store/auth.actions';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private store: Store, private snackBar: MatSnackBar) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Don't show error UI for /api/auth/me — AppComponent handles it silently
        const isAuthMe = request.url.includes('/api/auth/me');

        switch (error.status) {
          case 401:
            // Don't trigger refresh for auth endpoints - AppComponent handles /me errors itself
            if (!request.url.includes('/api/auth/refresh') && !isAuthMe) {
              this.store.dispatch(refreshToken());
            }
            break;

          case 403:
            if (!isAuthMe) {
              this.snackBar.open('Access denied. You do not have permission.', 'Close', {
                duration: 5000,
                panelClass: ['error-snackbar'],
              });
            }
            break;

          case 400:
          case 404:
          case 409:
          case 422:
            if (!isAuthMe) {
              const apiError = error.error;
              if (apiError?.fieldErrors?.length) {
                const messages = apiError.fieldErrors
                  .map((fe: any) => `${fe.field}: ${fe.message}`)
                  .join('\n');
                this.snackBar.open(messages, 'Close', {
                  duration: 8000,
                  panelClass: ['error-snackbar'],
                });
              } else if (apiError?.message) {
                this.snackBar.open(apiError.message, 'Close', {
                  duration: 5000,
                  panelClass: ['error-snackbar'],
                });
              }
            }
            break;

          default:
            if (error.status >= 500 && !isAuthMe) {
              this.snackBar.open(
                'An unexpected error occurred. Please try again later.',
                'Close',
                {
                  duration: 5000,
                  panelClass: ['error-snackbar'],
                }
              );
            }
            break;
        }

        return throwError(() => error);
      })
    );
  }
}
