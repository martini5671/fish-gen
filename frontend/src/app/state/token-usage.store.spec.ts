import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ToastrService } from 'ngx-toastr';
import { provideApiConfiguration } from '../api/api-configuration';
import { TokenUsageStore } from './token-usage.store';

describe('TokenUsageStore', () => {
  const toastr = { error: vi.fn() };
  let http: HttpTestingController;

  function setup(): InstanceType<typeof TokenUsageStore> {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApiConfiguration(''),
        { provide: ToastrService, useValue: toastr },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    return TestBed.inject(TokenUsageStore);
  }

  afterEach(() => {
    http.verify();
    vi.clearAllMocks();
  });

  it('does not fetch until loadUsage is called', () => {
    const store = setup();

    http.expectNone('/api/v1/usage');
    expect(store.aiCredits()).toBeNull();
  });

  it('stores the remaining AI credits', () => {
    const store = setup();

    store.loadUsage();
    http.expectOne('/api/v1/usage').flush({ aiCredits: 42 });

    expect(store.aiCredits()).toBe(42);
  });

  it('shows the backend error detail when loading fails', () => {
    const store = setup();

    store.loadUsage();
    http
      .expectOne('/api/v1/usage')
      .flush({ detail: 'Usage unavailable' }, { status: 500, statusText: 'Server Error' });

    expect(store.aiCredits()).toBeNull();
    expect(toastr.error).toHaveBeenCalledWith('Usage unavailable', 'Loading failed');
  });

  it('clears the credits on reset', () => {
    const store = setup();
    store.loadUsage();
    http.expectOne('/api/v1/usage').flush({ aiCredits: 7 });

    store.reset();

    expect(store.aiCredits()).toBeNull();
  });
});
