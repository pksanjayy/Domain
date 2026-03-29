import { LoadingService } from './loading.service';

describe('LoadingService', () => {
  let service: LoadingService;

  beforeEach(() => {
    service = new LoadingService();
  });

  it('should start with loading as false', (done) => {
    service.loading$.subscribe((loading) => {
      expect(loading).toBeFalse();
      done();
    });
  });

  it('should emit true when show() is called', (done) => {
    service.show();

    service.loading$.subscribe((loading) => {
      expect(loading).toBeTrue();
      done();
    });
  });

  it('should emit false when hide() is called after show()', (done) => {
    service.show();
    service.hide();

    service.loading$.subscribe((loading) => {
      expect(loading).toBeFalse();
      done();
    });
  });

  it('should remain true when multiple requests are active', (done) => {
    service.show(); // request 1
    service.show(); // request 2
    service.hide(); // request 1 done

    service.loading$.subscribe((loading) => {
      expect(loading).toBeTrue();
      done();
    });
  });

  it('should emit false only when all concurrent requests complete', (done) => {
    service.show();
    service.show();
    service.show();
    service.hide();
    service.hide();
    service.hide();

    service.loading$.subscribe((loading) => {
      expect(loading).toBeFalse();
      done();
    });
  });

  it('should not go below zero active requests', (done) => {
    service.hide();
    service.hide();
    service.hide();

    service.loading$.subscribe((loading) => {
      expect(loading).toBeFalse();
      done();
    });
  });

  it('should recover after going to zero and starting new request', (done) => {
    service.show();
    service.hide();
    service.show();

    service.loading$.subscribe((loading) => {
      expect(loading).toBeTrue();
      done();
    });
  });
});
