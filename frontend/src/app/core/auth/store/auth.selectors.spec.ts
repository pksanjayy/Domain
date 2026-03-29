import { AuthState, initialAuthState } from './auth.reducer';
import {
  selectAuthState,
  selectCurrentUser,
  selectAccessToken,
  selectIsAuthenticated,
  selectAuthIsLoading,
  selectAuthError,
  selectUserRole,
  selectUserMenus,
  selectUserPermissions,
} from './auth.selectors';
import { User } from '../../models';

describe('Auth Selectors', () => {
  const mockUser: User = {
    id: 1,
    username: 'admin',
    email: 'admin@test.com',
    role: 'SUPER_ADMIN',
    branchName: 'HQ',
    forcePasswordChange: false,
    menus: [
      { id: 1, name: 'Dashboard', icon: 'dashboard', path: '/dashboard', displayOrder: 1, parentId: null, children: [] },
    ],
    permissions: [
      { id: 1, moduleName: 'INVENTORY', canCreate: true, canRead: true, canUpdate: true, canDelete: false },
    ],
  };

  const loggedInState: AuthState = {
    user: mockUser,
    accessToken: 'jwt-token',
    isLoading: false,
    error: null,
  };

  const appState = { auth: loggedInState };
  const emptyState = { auth: initialAuthState };

  // --- selectAuthState ---

  it('should select the auth feature state', () => {
    expect(selectAuthState(appState)).toEqual(loggedInState);
  });

  // --- selectCurrentUser ---

  it('should select the current user', () => {
    expect(selectCurrentUser(appState)).toEqual(mockUser);
  });

  it('should return null when no user is logged in', () => {
    expect(selectCurrentUser(emptyState)).toBeNull();
  });

  // --- selectAccessToken ---

  it('should select the access token', () => {
    expect(selectAccessToken(appState)).toBe('jwt-token');
  });

  it('should return null when no token exists', () => {
    expect(selectAccessToken(emptyState)).toBeNull();
  });

  // --- selectIsAuthenticated ---

  it('should return true when user and token exist', () => {
    expect(selectIsAuthenticated(appState)).toBeTrue();
  });

  it('should return false when user is null', () => {
    const stateWithToken = { auth: { ...initialAuthState, accessToken: 'token' } };
    expect(selectIsAuthenticated(stateWithToken)).toBeFalse();
  });

  it('should return false when token is null', () => {
    const stateWithUser = { auth: { ...initialAuthState, user: mockUser } };
    expect(selectIsAuthenticated(stateWithUser)).toBeFalse();
  });

  it('should return false when both are null', () => {
    expect(selectIsAuthenticated(emptyState)).toBeFalse();
  });

  // --- selectAuthIsLoading ---

  it('should return loading state', () => {
    const loadingState = { auth: { ...initialAuthState, isLoading: true } };
    expect(selectAuthIsLoading(loadingState)).toBeTrue();
  });

  it('should return false when not loading', () => {
    expect(selectAuthIsLoading(emptyState)).toBeFalse();
  });

  // --- selectAuthError ---

  it('should return error message', () => {
    const errorState = { auth: { ...initialAuthState, error: 'Bad credentials' } };
    expect(selectAuthError(errorState)).toBe('Bad credentials');
  });

  it('should return null when no error', () => {
    expect(selectAuthError(emptyState)).toBeNull();
  });

  // --- selectUserRole ---

  it('should return user role', () => {
    expect(selectUserRole(appState)).toBe('SUPER_ADMIN');
  });

  it('should return null when no user is logged in', () => {
    expect(selectUserRole(emptyState)).toBeNull();
  });

  // --- selectUserMenus ---

  it('should return user menus', () => {
    const menus = selectUserMenus(appState);
    expect(menus.length).toBe(1);
    expect(menus[0].name).toBe('Dashboard');
  });

  it('should return empty array when no user', () => {
    expect(selectUserMenus(emptyState)).toEqual([]);
  });

  // --- selectUserPermissions ---

  it('should return user permissions', () => {
    const perms = selectUserPermissions(appState);
    expect(perms.length).toBe(1);
    expect(perms[0].moduleName).toBe('INVENTORY');
  });

  it('should return empty array when no user', () => {
    expect(selectUserPermissions(emptyState)).toEqual([]);
  });
});
