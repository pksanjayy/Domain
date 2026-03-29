import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { LoadingInterceptor } from './loading.interceptor';
import { LoadingService } from '../services/loading.service';

describe('LoadingInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let loadingService: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        LoadingService,
        { provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true },
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    loadingService = TestBed.inject(LoadingService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should call show() when request starts', () => {
    spyOn(loadingService, 'show');

    httpClient.get('/api/test').subscribe();

    expect(loadingService.show).toHaveBeenCalled();

    httpMock.expectOne('/api/test').flush({});
  });

  it('should call hide() when request completes', () => {
    spyOn(loadingService, 'hide');

    httpClient.get('/api/test').subscribe();

    const req = httpMock.expectOne('/api/test');
    req.flush({});

    expect(loadingService.hide).toHaveBeenCalled();
  });

  it('should call hide() when request errors', () => {
    spyOn(loadingService, 'hide');

    httpClient.get('/api/test').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/test');
    req.flush({}, { status: 500, statusText: 'Error' });

    expect(loadingService.hide).toHaveBeenCalled();
  });
});
