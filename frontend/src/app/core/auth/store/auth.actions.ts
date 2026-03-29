import { createAction, props } from '@ngrx/store';
import { User } from '../../models';
import { LoginRequest } from '../auth.service';

// Login
export const login = createAction(
  '[Auth] Login',
  props<{ request: LoginRequest }>()
);

export const loginSuccess = createAction(
  '[Auth] Login Success',
  props<{ user: User; accessToken: string }>()
);

export const loginFailure = createAction(
  '[Auth] Login Failure',
  props<{ error: string }>()
);

// Logout
export const logout = createAction('[Auth] Logout');

// Refresh Token
export const refreshToken = createAction('[Auth] Refresh Token');

export const refreshTokenSuccess = createAction(
  '[Auth] Refresh Token Success',
  props<{ user: User; accessToken: string }>()
);

export const refreshTokenFailure = createAction(
  '[Auth] Refresh Token Failure'
);

// Load user from stored token (app init)
export const loadUser = createAction(
  '[Auth] Load User',
  props<{ user: User; accessToken: string }>()
);

// Signal that auth initialization is complete (no token case)
export const initComplete = createAction('[Auth] Init Complete');
