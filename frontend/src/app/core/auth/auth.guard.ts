import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, combineLatest } from 'rxjs';
import { filter, map, take } from 'rxjs/operators';
import { selectIsAuthenticated, selectAuthInitialized } from './store/auth.selectors';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private store: Store, private router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    return combineLatest([
      this.store.select(selectAuthInitialized),
      this.store.select(selectIsAuthenticated),
    ]).pipe(
      filter(([initialized]) => initialized),
      take(1),
      map(([, isAuthenticated]) => {
        if (isAuthenticated) {
          return true;
        }
        return this.router.createUrlTree(['/login']);
      })
    );
  }
}
