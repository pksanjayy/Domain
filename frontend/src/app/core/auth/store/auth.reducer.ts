import { createReducer, on } from '@ngrx/store';
import { User } from '../../models';
import * as AuthActions from './auth.actions';

export interface AuthState {
  user: User | null;
  accessToken: string | null;
  isLoading: boolean;
  error: string | null;
  initialized: boolean;
}

export const initialAuthState: AuthState = {
  user: null,
  accessToken: null,
  isLoading: false,
  error: null,
  initialized: false,
};

export const authReducer = createReducer(
  initialAuthState,

  on(AuthActions.login, (state) => ({
    ...state,
    isLoading: true,
    error: null,
  })),

  on(AuthActions.loginSuccess, (state, { user, accessToken }) => ({
    ...state,
    user,
    accessToken,
    isLoading: false,
    error: null,
    initialized: true,
  })),

  on(AuthActions.loginFailure, (state, { error }) => ({
    ...state,
    isLoading: false,
    error,
  })),

  on(AuthActions.logout, () => ({
    ...initialAuthState,
    initialized: true,
  })),

  on(AuthActions.refreshTokenSuccess, (state, { user, accessToken }) => ({
    ...state,
    user,
    accessToken,
    initialized: true,
  })),

  on(AuthActions.refreshTokenFailure, () => ({
    ...initialAuthState,
    initialized: true,
  })),

  on(AuthActions.loadUser, (state, { user, accessToken }) => ({
    ...state,
    user,
    accessToken,
    isLoading: false,
    error: null,
    initialized: true,
  })),

  on(AuthActions.initComplete, (state) => ({
    ...state,
    initialized: true,
  }))
);
