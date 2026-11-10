import { TestBed } from '@angular/core/testing';
import { SETTINGS_STORAGE_KEY, SettingsStore } from './settings.store';

describe('SettingsStore', () => {
  function setup(): InstanceType<typeof SettingsStore> {
    const store = TestBed.inject(SettingsStore);
    TestBed.tick();
    return store;
  }

  function persisted() {
    return JSON.parse(localStorage.getItem(SETTINGS_STORAGE_KEY)!);
  }

  beforeEach(() => localStorage.clear());

  it('starts with the default gradients, dark front text and white back text', () => {
    const store = setup();

    expect(store.frontBackground()).toBeNull();
    expect(store.backBackground()).toBeNull();
    expect(store.frontText()).toBe('dark');
    expect(store.backText()).toBe('light');
  });

  it('saves every change to local storage', () => {
    const store = setup();

    store.setFrontBackground('#ff0000');
    store.setBackText('dark');
    TestBed.tick();

    expect(persisted()).toEqual({
      frontBackground: '#ff0000',
      backBackground: null,
      frontText: 'dark',
      backText: 'dark',
    });
  });

  it('restores the saved settings', () => {
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

  it('ignores saved values of the wrong shape', () => {
    localStorage.setItem(SETTINGS_STORAGE_KEY, JSON.stringify({ frontBackground: 42, frontText: 'purple' }));

    const store = setup();

    expect(store.frontBackground()).toBeNull();
    expect(store.frontText()).toBe('dark');
  });

  it('ignores saved settings that are not valid JSON', () => {
    localStorage.setItem(SETTINGS_STORAGE_KEY, '{not json');

    const store = setup();

    expect(store.backText()).toBe('light');
  });

  it('goes back to the defaults on reset', () => {
    const store = setup();
    store.setFrontBackground('#ff0000');
    store.setFrontText('light');

    store.reset();
    TestBed.tick();

    expect(store.frontBackground()).toBeNull();
    expect(store.frontText()).toBe('dark');
    expect(persisted().frontBackground).toBeNull();
  });
});
