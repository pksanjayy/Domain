import { authReducer, initialAuthState, AuthState } from './auth.reducer';
import * as AuthActions from './auth.actions';
import { User } from '../../models';

describe('Auth Reducer', () => {
  const mockUser: User = {
    id: 1,
    username: 'admin',
    email: 'admin@test.com',
    role: 'SUPER_ADMIN',
    branchName: 'HQ',
    forcePasswordChange: false,
    menus: [],
    permissions: [],
  };

  it('should return the initial state for an unknown action', () => {
    const action = { type: 'UNKNOWN' } as any;
    const state = authReducer(initialAuthState, action);
    expect(state).toEqual(initialAuthState);
  });

  // --- login ---

  it('should set isLoading to true and clear error on login', () => {
    const previousState: AuthState = { ...initialAuthState, error: 'old error' };
    const action = AuthActions.login({ request: { username: 'admin', password: 'pass' } });
    const state = authReducer(previousState, action);

    expect(state.isLoading).toBeTrue();
    expect(state.error).toBeNull();
    expect(state.user).toBeNull();
  });

  it('should set user and accessToken on loginSuccess', () => {
    const loadingState: AuthState = { ...initialAuthState, isLoading: true };
    const action = AuthActions.loginSuccess({ user: mockUser, accessToken: 'jwt-token' });
    const state = authReducer(loadingState, action);

    expect(state.user).toEqual(mockUser);
    expect(state.accessToken).toBe('jwt-token');
    expect(state.isLoading).toBeFalse();
    expect(state.error).toBeNull();
  });

  it('should set error and clear isLoading on loginFailure', () => {
    const loadingState: AuthState = { ...initialAuthState, isLoading: true };
    const action = AuthActions.loginFailure({ error: 'Invalid credentials' });
    const state = authReducer(loadingState, action);

    expect(state.error).toBe('Invalid credentials');
    expect(state.isLoading).toBeFalse();
    expect(state.user).toBeNull();
  });

  // --- logout ---

  it('should reset to initial state on logout', () => {
    const loggedInState: AuthState = {
      user: mockUser,
      accessToken: 'jwt-token',
      isLoading: false,
      error: null,
    };
    const action = AuthActions.logout();
    const state = authReducer(loggedInState, action);

    expect(state).toEqual(initialAuthState);
  });

  // --- refreshTokenSuccess ---

  it('should update user and accessToken on refreshTokenSuccess', () => {
    const existingState: AuthState = {
      user: mockUser,
      accessToken: 'old-token',
      isLoading: false,
      error: null,
    };
    const updatedUser = { ...mockUser, email: 'new@test.com' };
    const action = AuthActions.refreshTokenSuccess({ user: updatedUser, accessToken: 'new-token' });
    const state = authReducer(existingState, action);

    expect(state.accessToken).toBe('new-token');
    expect(state.user?.email).toBe('new@test.com');
  });

  // --- refreshTokenFailure ---

  it('should reset to initial state on refreshTokenFailure', () => {
    const loggedInState: AuthState = {
      user: mockUser,
      accessToken: 'jwt-token',
      isLoading: false,
      error: null,
    };
    const action = AuthActions.refreshTokenFailure();
    const state = authReducer(loggedInState, action);

    expect(state).toEqual(initialAuthState);
  });

  // --- loadUser ---

  it('should set user and accessToken on loadUser', () => {
    const action = AuthActions.loadUser({ user: mockUser, accessToken: 'restored-token' });
    const state = authReducer(initialAuthState, action);

    expect(state.user).toEqual(mockUser);
    expect(state.accessToken).toBe('restored-token');
    expect(state.isLoading).toBeFalse();
    expect(state.error).toBeNull();
  });

  it('should clear previous loading and error state on loadUser', () => {
    const errorState: AuthState = { ...initialAuthState, isLoading: true, error: 'prev error' };
    const action = AuthActions.loadUser({ user: mockUser, accessToken: 'token' });
    const state = authReducer(errorState, action);

    expect(state.isLoading).toBeFalse();
    expect(state.error).toBeNull();
  });
});
