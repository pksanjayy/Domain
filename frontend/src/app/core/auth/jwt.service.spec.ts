import { JwtService } from './jwt.service';

describe('JwtService', () => {
  let service: JwtService;

  // Helper to create a JWT-like token with a given payload
  function createToken(payload: object): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const body = btoa(JSON.stringify(payload));
    return `${header}.${body}.fake-signature`;
  }

  beforeEach(() => {
    service = new JwtService();
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  // --- getToken / setToken / removeToken ---

  it('should return null when no token is stored', () => {
    expect(service.getToken()).toBeNull();
  });

  it('should store and retrieve a token', () => {
    service.setToken('my-jwt-token');
    expect(service.getToken()).toBe('my-jwt-token');
  });

  it('should overwrite an existing token', () => {
    service.setToken('first-token');
    service.setToken('second-token');
    expect(service.getToken()).toBe('second-token');
  });

  it('should remove a stored token', () => {
    service.setToken('my-jwt-token');
    service.removeToken();
    expect(service.getToken()).toBeNull();
  });

  it('should not throw when removing a non-existent token', () => {
    expect(() => service.removeToken()).not.toThrow();
  });

  // --- isTokenExpired ---

  it('should return true for an expired token', () => {
    const pastExp = Math.floor(Date.now() / 1000) - 3600; // 1 hour ago
    const token = createToken({ exp: pastExp, sub: 'admin' });
    expect(service.isTokenExpired(token)).toBeTrue();
  });

  it('should return false for a valid (non-expired) token', () => {
    const futureExp = Math.floor(Date.now() / 1000) + 3600; // 1 hour from now
    const token = createToken({ exp: futureExp, sub: 'admin' });
    expect(service.isTokenExpired(token)).toBeFalse();
  });

  it('should return true for a malformed token', () => {
    expect(service.isTokenExpired('not-a-jwt')).toBeTrue();
  });

  it('should return true for an empty string token', () => {
    expect(service.isTokenExpired('')).toBeTrue();
  });

  it('should return true for a token with invalid base64', () => {
    expect(service.isTokenExpired('header.!!!invalid!!!.sig')).toBeTrue();
  });

  // --- getTokenPayload ---

  it('should decode a valid token payload', () => {
    const payload = { sub: 'admin', role: 'SUPER_ADMIN', exp: 999999 };
    const token = createToken(payload);
    const decoded = service.getTokenPayload(token);
    expect(decoded.sub).toBe('admin');
    expect(decoded.role).toBe('SUPER_ADMIN');
    expect(decoded.exp).toBe(999999);
  });

  it('should return null for a malformed token', () => {
    expect(service.getTokenPayload('bad-token')).toBeNull();
  });

  it('should return null for an empty string', () => {
    expect(service.getTokenPayload('')).toBeNull();
  });

  it('should return null for a token with only one segment', () => {
    expect(service.getTokenPayload('only-header')).toBeNull();
  });
});
