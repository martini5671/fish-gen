import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthStore } from './auth.store';

describe('authInterceptor', () => {
  const store = { token: signal<string | null>(null), logout: vi.fn() };
  let http: HttpClient;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    store.token.set(null);
    store.logout.mockReset();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthStore, useValue: store },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('adds the bearer token to API calls', () => {
    store.token.set('abc');
    http.get('/api/v1/collections').subscribe();

    expect(httpTesting.expectOne('/api/v1/collections').request.headers.get('Authorization')).toBe(
      'Bearer abc',
    );
  });

  it('does not add the token to the login call', () => {
    store.token.set('abc');
    http.post('/api/v1/auth/login', {}).subscribe();

    expect(httpTesting.expectOne('/api/v1/auth/login').request.headers.has('Authorization')).toBe(
      false,
    );
  });

  it('logs out when an authenticated call returns 401', () => {
    store.token.set('expired');
    http.get('/api/v1/collections').subscribe({ error: () => {} });

    httpTesting
      .expectOne('/api/v1/collections')
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(store.logout).toHaveBeenCalled();
  });
});
