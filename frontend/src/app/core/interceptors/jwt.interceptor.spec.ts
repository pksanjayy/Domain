import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { JwtInterceptor } from './jwt.interceptor';
import { JwtService } from '../auth/jwt.service';

describe('JwtInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let jwtService: JwtService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        JwtService,
        { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    jwtService = TestBed.inject(JwtService);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should add Authorization header when token exists', () => {
    jwtService.setToken('my-jwt-token');

    httpClient.get('/api/vehicles').subscribe();

    const req = httpMock.expectOne('/api/vehicles');
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-jwt-token');
    req.flush({});
  });

  it('should NOT add Authorization header when no token exists', () => {
    httpClient.get('/api/vehicles').subscribe();

    const req = httpMock.expectOne('/api/vehicles');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('should NOT add Authorization header for /api/auth/login', () => {
    jwtService.setToken('my-jwt-token');

    httpClient.post('/api/auth/login', {}).subscribe();

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('should NOT add Authorization header for /api/auth/refresh', () => {
    jwtService.setToken('my-jwt-token');

    httpClient.post('/api/auth/refresh', {}).subscribe();

    const req = httpMock.expectOne('/api/auth/refresh');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('should add Authorization header for non-auth API endpoints', () => {
    jwtService.setToken('my-jwt-token');

    httpClient.get('/api/inventory/vehicles/filter').subscribe();

    const req = httpMock.expectOne('/api/inventory/vehicles/filter');
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-jwt-token');
    req.flush({});
  });

  it('should pass request through even without token', () => {
    httpClient.get('/api/dashboard').subscribe((response) => {
      expect(response).toEqual({ success: true });
    });

    const req = httpMock.expectOne('/api/dashboard');
    req.flush({ success: true });
  });
});
