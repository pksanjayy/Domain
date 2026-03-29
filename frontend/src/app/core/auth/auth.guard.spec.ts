import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { AuthGuard } from './auth.guard';
import { selectIsAuthenticated } from './store/auth.selectors';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let store: MockStore;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        provideMockStore({
          selectors: [{ selector: selectIsAuthenticated, value: false }],
        }),
        {
          provide: Router,
          useValue: { createUrlTree: jasmine.createSpy('createUrlTree').and.returnValue('/login') },
        },
      ],
    });

    guard = TestBed.inject(AuthGuard);
    store = TestBed.inject(MockStore);
    router = TestBed.inject(Router);
  });

  it('should allow access when user is authenticated', (done) => {
    store.overrideSelector(selectIsAuthenticated, true);
    store.refreshState();

    guard.canActivate().subscribe((result) => {
      expect(result).toBeTrue();
      done();
    });
  });

  it('should redirect to /login when user is not authenticated', (done) => {
    store.overrideSelector(selectIsAuthenticated, false);
    store.refreshState();

    guard.canActivate().subscribe((result) => {
      expect(result).toBe('/login' as any);
      expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
      done();
    });
  });
});
