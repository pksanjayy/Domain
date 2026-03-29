import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService, LoginRequest, LoginResponse } from './auth.service';
import { JwtService } from './jwt.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let jwtService: JwtService;

  const mockUser = {
    id: 1,
    username: 'admin',
    email: 'admin@test.com',
    role: 'SUPER_ADMIN',
    branchName: 'Main Branch',
    forcePasswordChange: false,
    menus: [],
    permissions: [],
  };

  const mockLoginResponse: LoginResponse = {
    accessToken: 'mock-access-token',
    refreshToken: 'mock-refresh-token',
    user: mockUser,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService, JwtService],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    jwtService = TestBed.inject(JwtService);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  // --- login ---

  it('should POST to /api/auth/login with credentials', () => {
    const request: LoginRequest = { username: 'admin', password: 'Admin@123' };

    service.login(request).subscribe((response) => {
      expect(response.success).toBeTrue();
      expect(response.data.accessToken).toBe('mock-access-token');
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ success: true, data: mockLoginResponse });
  });

  it('should handle login failure', () => {
    const request: LoginRequest = { username: 'admin', password: 'wrong' };

    service.login(request).subscribe({
      error: (err) => {
        expect(err.status).toBe(401);
      },
    });

    const req = httpMock.expectOne('/api/auth/login');
    req.flush({ success: false, message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });
  });

  // --- refreshToken ---

  it('should POST to /api/auth/refresh with refresh token', () => {
    service.refreshToken('my-refresh-token').subscribe((response) => {
      expect(response.data.accessToken).toBe('mock-access-token');
    });

    const req = httpMock.expectOne('/api/auth/refresh');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ refreshToken: 'my-refresh-token' });
    req.flush({ success: true, data: mockLoginResponse });
  });

  // --- logout ---

  it('should remove access token and refresh token on logout', () => {
    jwtService.setToken('access-token');
    localStorage.setItem('dms_refresh_token', 'refresh-token');

    service.logout();

    expect(jwtService.getToken()).toBeNull();
    expect(localStorage.getItem('dms_refresh_token')).toBeNull();
  });

  it('should not throw when logging out with no tokens', () => {
    expect(() => service.logout()).not.toThrow();
  });

  // --- storeTokens ---

  it('should store both access and refresh tokens', () => {
    service.storeTokens(mockLoginResponse);

    expect(jwtService.getToken()).toBe('mock-access-token');
    expect(localStorage.getItem('dms_refresh_token')).toBe('mock-refresh-token');
  });

  // --- getRefreshToken ---

  it('should return stored refresh token', () => {
    localStorage.setItem('dms_refresh_token', 'stored-refresh');
    expect(service.getRefreshToken()).toBe('stored-refresh');
  });

  it('should return null when no refresh token exists', () => {
    expect(service.getRefreshToken()).toBeNull();
  });
});
