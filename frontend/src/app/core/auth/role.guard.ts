import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router, UrlTree } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { selectUserRole } from './store/auth.selectors';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  constructor(private store: Store, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean | UrlTree> {
    const requiredRoles = route.data['roles'] as string[];
    if (!requiredRoles || requiredRoles.length === 0) {
      return new Observable((observer) => {
        observer.next(true);
        observer.complete();
      });
    }

    return this.store.select(selectUserRole).pipe(
      take(1),
      map((userRole) => {
        if (userRole && requiredRoles.includes(userRole)) {
          return true;
        }
        return this.router.createUrlTree(['/dashboard']);
      })
    );
  }
}
