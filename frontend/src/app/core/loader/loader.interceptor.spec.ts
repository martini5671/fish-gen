import { HttpClient, HttpContext, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { loaderInterceptor, SKIP_LOADER } from './loader.interceptor';
import { LoaderService } from './loader.service';

describe('loaderInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let loader: LoaderService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([loaderInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    loader = TestBed.inject(LoaderService);
  });
  afterEach(() => httpTesting.verify());

  it('shows the loader until every pending request has finished', () => {
    http.get('/a').subscribe();
    http.get('/b').subscribe({ error: () => {} });
    expect(loader.loading()).toBe(true);

    httpTesting.expectOne('/a').flush({});
    expect(loader.loading()).toBe(true);

    httpTesting.expectOne('/b').flush(null, { status: 500, statusText: 'Server Error' });
    expect(loader.loading()).toBe(false);
  });

  it('hides the loader when the request is cancelled', () => {
    const subscription = http.get('/a').subscribe();
    const req = httpTesting.expectOne('/a');
    expect(loader.loading()).toBe(true);

    subscription.unsubscribe();
    expect(req.cancelled).toBe(true);
    expect(loader.loading()).toBe(false);
  });

  it('skips the loader when the request opts out', () => {
    http.get('/a', { context: new HttpContext().set(SKIP_LOADER, true) }).subscribe();
    expect(loader.loading()).toBe(false);
    httpTesting.expectOne('/a').flush({});
  });
});
