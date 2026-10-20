import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { provideApiConfiguration } from '../../api/api-configuration';
import { AUTH_STORAGE_KEY, AuthStore } from './auth.store';

// header.{"sub":"test-user"}.signature
const TOKEN = `eyJhbGciOiJIUzI1NiJ9.${btoa(JSON.stringify({ sub: 'test-user' }))}.sig`;

describe('AuthStore', () => {
  const toastr = { error: vi.fn() };
  let http: HttpTestingController;

  function setup(): InstanceType<typeof AuthStore> {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideApiConfiguration(''),
        { provide: ToastrService, useValue: toastr },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    return TestBed.inject(AuthStore);
  }

  beforeEach(() => {
    localStorage.clear();
    toastr.error.mockReset();
  });
  afterEach(() => http.verify());

  it('stores the token after a successful login', () => {
    const store = setup();

    store.login({ username: 'test-user', password: 'password' });
    expect(store.loading()).toBe(true);
    const req = http.expectOne('/api/v1/auth/login');
    expect(req.request.body).toEqual({ username: 'test-user', password: 'password' });
    req.flush({ accessToken: TOKEN, tokenType: 'Bearer', expiresIn: 3600 });
    TestBed.tick();

    expect(store.token()).toBe(TOKEN);
    expect(store.username()).toBe('test-user');
    expect(store.isAuthenticated()).toBe(true);
    expect(store.loading()).toBe(false);
    expect(JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY)!).token).toBe(TOKEN);
    expect(TestBed.inject(Router).navigateByUrl).toHaveBeenCalledWith('/');
  });

  it('shows a toast and stays logged out when credentials are rejected', () => {
    const store = setup();

    store.login({ username: 'test-user', password: 'wrong' });
    http
      .expectOne('/api/v1/auth/login')
      .flush(
        { title: 'Authentication failed', detail: 'Invalid username or password' },
        { status: 401, statusText: 'Unauthorized' },
      );

    expect(toastr.error).toHaveBeenCalledWith('Invalid username or password', 'Login failed');

    expect(store.isAuthenticated()).toBe(false);
    expect(store.loading()).toBe(false);
  });

  it('clears the session on logout', () => {
    localStorage.setItem(
      AUTH_STORAGE_KEY,
      JSON.stringify({ token: TOKEN, expiresAt: Date.now() + 60_000, username: 'test-user' }),
    );
    const store = setup();
    TestBed.tick();
    expect(store.isAuthenticated()).toBe(true);

    store.logout();
    TestBed.tick();

    expect(store.token()).toBeNull();
    expect(localStorage.getItem(AUTH_STORAGE_KEY)).toBeNull();
    expect(TestBed.inject(Router).navigateByUrl).toHaveBeenCalledWith('/login');
  });

  it('does not restore an expired token', () => {
    localStorage.setItem(
      AUTH_STORAGE_KEY,
      JSON.stringify({ token: TOKEN, expiresAt: Date.now() - 1, username: 'test-user' }),
    );
    const store = setup();

    expect(store.token()).toBeNull();
    expect(store.isAuthenticated()).toBe(false);
    expect(localStorage.getItem(AUTH_STORAGE_KEY)).toBeNull();
  });
});
