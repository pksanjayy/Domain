import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AuthState } from './auth.reducer';
import { RoleName } from '../../models/user.model';

export const selectAuthState = createFeatureSelector<AuthState>('auth');

export const selectCurrentUser = createSelector(
  selectAuthState,
  (state) => state.user
);

export const selectAccessToken = createSelector(
  selectAuthState,
  (state) => state.accessToken
);

export const selectAuthInitialized = createSelector(
  selectAuthState,
  (state) => state.initialized
);

export const selectIsAuthenticated = createSelector(
  selectAuthState,
  (state) => !!state.user && !!state.accessToken
);

export const selectAuthIsLoading = createSelector(
  selectAuthState,
  (state) => state.isLoading
);

export const selectAuthError = createSelector(
  selectAuthState,
  (state) => state.error
);

export const selectUserRole = createSelector(
  selectCurrentUser,
  (user) => (user?.roles?.[0] as RoleName) ?? null
);

export const selectUserMenus = createSelector(
  selectCurrentUser,
  (user) => user?.menus ?? []
);

export const selectUserPermissions = createSelector(
  selectCurrentUser,
  (user) => user?.permissions ?? []
);
