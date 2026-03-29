import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { RoleGuard } from './role.guard';
import { selectUserRole } from './store/auth.selectors';

describe('RoleGuard', () => {
  let guard: RoleGuard;
  let store: MockStore;
  let router: Router;

  function createRoute(roles: string[]): ActivatedRouteSnapshot {
    return { data: { roles } } as any;
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        RoleGuard,
        provideMockStore({
          selectors: [{ selector: selectUserRole, value: null }],
        }),
        {
          provide: Router,
          useValue: { createUrlTree: jasmine.createSpy('createUrlTree').and.returnValue('/dashboard') },
        },
      ],
    });

    guard = TestBed.inject(RoleGuard);
    store = TestBed.inject(MockStore);
    router = TestBed.inject(Router);
  });

  it('should allow access when no roles are required', (done) => {
    const route = createRoute([]);

    guard.canActivate(route).subscribe((result) => {
      expect(result).toBeTrue();
      done();
    });
  });

  it('should allow access when roles data is undefined', (done) => {
    const route = { data: {} } as any;

    guard.canActivate(route).subscribe((result) => {
      expect(result).toBeTrue();
      done();
    });
  });

  it('should allow access when user has matching role', (done) => {
    store.overrideSelector(selectUserRole, 'SUPER_ADMIN');
    store.refreshState();

    const route = createRoute(['SUPER_ADMIN', 'MASTER_USER']);

    guard.canActivate(route).subscribe((result) => {
      expect(result).toBeTrue();
      done();
    });
  });

  it('should redirect to /dashboard when user role does not match', (done) => {
    store.overrideSelector(selectUserRole, 'WORKSHOP_EXEC');
    store.refreshState();

    const route = createRoute(['SUPER_ADMIN']);

    guard.canActivate(route).subscribe((result) => {
      expect(result).toBe('/dashboard' as any);
      expect(router.createUrlTree).toHaveBeenCalledWith(['/dashboard']);
      done();
    });
  });

  it('should redirect to /dashboard when user role is null', (done) => {
    store.overrideSelector(selectUserRole, null as any);
    store.refreshState();

    const route = createRoute(['SUPER_ADMIN']);

    guard.canActivate(route).subscribe((result) => {
      expect(result).toBe('/dashboard' as any);
      done();
    });
  });
});
