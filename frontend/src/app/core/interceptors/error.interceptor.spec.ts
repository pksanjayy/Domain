import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ErrorInterceptor } from './error.interceptor';

describe('ErrorInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let store: MockStore;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(() => {
    const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        provideMockStore(),
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    store = TestBed.inject(MockStore);
    snackBar = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;

    spyOn(store, 'dispatch');
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should dispatch refreshToken action on 401 for non-refresh URLs', () => {
    httpClient.get('/api/vehicles').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/vehicles');
    req.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(store.dispatch).toHaveBeenCalled();
  });

  it('should NOT dispatch refreshToken on 401 for /api/auth/refresh', () => {
    httpClient.post('/api/auth/refresh', {}).subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/auth/refresh');
    req.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(store.dispatch).not.toHaveBeenCalled();
  });

  it('should show snackbar on 403 Forbidden', () => {
    httpClient.get('/api/admin/users').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/admin/users');
    req.flush({}, { status: 403, statusText: 'Forbidden' });

    expect(snackBar.open).toHaveBeenCalledWith(
      'Access denied. You do not have permission.',
      'Close',
      jasmine.objectContaining({ duration: 5000 })
    );
  });

  it('should show field error messages on 400 with fieldErrors', () => {
    httpClient.post('/api/vehicles', {}).subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/vehicles');
    req.flush(
      {
        fieldErrors: [
          { field: 'vin', message: 'VIN is required' },
          { field: 'brand', message: 'Brand is required' },
        ],
      },
      { status: 400, statusText: 'Bad Request' }
    );

    expect(snackBar.open).toHaveBeenCalledWith(
      jasmine.stringContaining('vin: VIN is required'),
      'Close',
      jasmine.objectContaining({ duration: 8000 })
    );
  });

  it('should show generic message on 400 without fieldErrors', () => {
    httpClient.post('/api/vehicles', {}).subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/vehicles');
    req.flush(
      { message: 'Validation failed' },
      { status: 400, statusText: 'Bad Request' }
    );

    expect(snackBar.open).toHaveBeenCalledWith('Validation failed', 'Close', jasmine.any(Object));
  });

  it('should show snackbar on 404 with message', () => {
    httpClient.get('/api/vehicles/999').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/vehicles/999');
    req.flush({ message: 'Vehicle not found' }, { status: 404, statusText: 'Not Found' });

    expect(snackBar.open).toHaveBeenCalledWith('Vehicle not found', 'Close', jasmine.any(Object));
  });

  it('should show generic error on 500', () => {
    httpClient.get('/api/dashboard').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/dashboard');
    req.flush({}, { status: 500, statusText: 'Internal Server Error' });

    expect(snackBar.open).toHaveBeenCalledWith(
      'An unexpected error occurred. Please try again later.',
      'Close',
      jasmine.any(Object)
    );
  });

  it('should re-throw the error to the caller', () => {
    let receivedError: any;
    httpClient.get('/api/test').subscribe({ error: (err) => (receivedError = err) });

    const req = httpMock.expectOne('/api/test');
    req.flush({}, { status: 422, statusText: 'Unprocessable' });

    expect(receivedError).toBeTruthy();
    expect(receivedError.status).toBe(422);
  });
});
