import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models';
import { User } from '../models';
import { JwtService } from './jwt.service';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface RefreshRequest {
  refreshToken: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = '/api/auth';

  constructor(
    private http: HttpClient,
    private jwtService: JwtService
  ) {}

  login(request: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(
      `${this.baseUrl}/login`,
      request
    );
  }

  refreshToken(refreshToken: string): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(
      `${this.baseUrl}/refresh`,
      { refreshToken } as RefreshRequest
    );
  }

  getMe(): Observable<ApiResponse<User>> {
    return this.http.get<ApiResponse<User>>(`${this.baseUrl}/me`);
  }

  logout(): void {
    this.jwtService.removeToken();
    localStorage.removeItem('dms_refresh_token');
  }

  storeTokens(response: LoginResponse): void {
    this.jwtService.setToken(response.accessToken);
    localStorage.setItem('dms_refresh_token', response.refreshToken);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('dms_refresh_token');
  }
}
