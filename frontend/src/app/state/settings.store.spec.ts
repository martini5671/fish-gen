import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ToastrService } from 'ngx-toastr';
import { provideApiConfiguration } from '../api/api-configuration';
import { SETTINGS_STORAGE_KEY, SettingsStore } from './settings.store';

describe('SettingsStore', () => {
  const toastr = { error: vi.fn() };
  let http: HttpTestingController;

  function setup(): InstanceType<typeof SettingsStore> {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApiConfiguration(''),
        { provide: ToastrService, useValue: toastr },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    return TestBed.inject(SettingsStore);
  }

  function persisted() {
    const raw = localStorage.getItem(SETTINGS_STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  beforeEach(() => localStorage.clear());

  afterEach(() => {
    http.verify();
    vi.clearAllMocks();
  });

  it('starts with the default gradients, dark front text and white back text', () => {
    const store = setup();

    expect(store.frontBackground()).toBeNull();
    expect(store.backBackground()).toBeNull();
    expect(store.frontText()).toBe('dark');
    expect(store.backText()).toBe('light');
  });

  it('restores the cached settings from local storage', () => {
    localStorage.setItem(
      SETTINGS_STORAGE_KEY,
      JSON.stringify({ frontBackground: '#00ff00', backBackground: '#0000ff', frontText: 'light', backText: 'dark' }),
    );

    const store = setup();

    expect(store.frontBackground()).toBe('#00ff00');
    expect(store.backBackground()).toBe('#0000ff');
    expect(store.frontText()).toBe('light');
    expect(store.backText()).toBe('dark');
  });

  it('ignores a cached entry that is not valid JSON', () => {
    localStorage.setItem(SETTINGS_STORAGE_KEY, '{not json');

    expect(setup().backText()).toBe('light');
  });

  it('does not write to local storage on a plain change', () => {
    const store = setup();

    store.setFrontBackground('#ff0000');

    expect(persisted()).toBeNull();
  });

  it('loads the settings from the backend and caches them', () => {
    const store = setup();

    store.loadSettings();
    http.expectOne('/api/v1/settings').flush({ frontBackground: '#123456', frontText: 'light', backText: 'dark' });

    expect(store.frontBackground()).toBe('#123456');
    expect(store.backBackground()).toBeNull();
    expect(store.frontText()).toBe('light');
    expect(persisted()).toEqual({
      frontBackground: '#123456',
      backBackground: null,
      frontText: 'light',
      backText: 'dark',
    });
  });

  it('keeps the cached settings and shows the error when loading fails', () => {
    localStorage.setItem(SETTINGS_STORAGE_KEY, JSON.stringify({ frontBackground: '#00ff00' }));
    const store = setup();

    store.loadSettings();
    http
      .expectOne('/api/v1/settings')
      .flush({ detail: 'Settings unavailable' }, { status: 500, statusText: 'Server Error' });

    expect(store.frontBackground()).toBe('#00ff00');
    expect(toastr.error).toHaveBeenCalledWith('Settings unavailable', 'Loading failed');
  });

  it('sends the current settings and caches them once the backend accepts them', () => {
    const store = setup();
    store.setFrontBackground('#ff0000');
    store.setBackText('dark');

    store.saveSettings();
    const request = http.expectOne('/api/v1/settings');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ frontBackground: '#ff0000', backBackground: undefined, frontText: 'dark', backText: 'dark' });
    expect(persisted()).toBeNull();

    request.flush(null);

    expect(persisted()).toEqual({ frontBackground: '#ff0000', backBackground: null, frontText: 'dark', backText: 'dark' });
  });

  it('does not cache the settings when saving fails', () => {
    const store = setup();
    store.setFrontText('light');

    store.saveSettings();
    http.expectOne('/api/v1/settings').flush({ detail: 'Nope' }, { status: 400, statusText: 'Bad Request' });

    expect(persisted()).toBeNull();
    expect(toastr.error).toHaveBeenCalledWith('Nope', 'Saving failed');
  });

  it('goes back to the defaults on reset', () => {
    const store = setup();
    store.setFrontBackground('#ff0000');
    store.setFrontText('light');

    store.reset();

    expect(store.frontBackground()).toBeNull();
    expect(store.frontText()).toBe('dark');
  });
});
