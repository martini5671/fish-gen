import { effect } from '@angular/core';
import { patchState, signalStore, withHooks, withMethods, withState } from '@ngrx/signals';

export const SETTINGS_STORAGE_KEY = 'fish-gen.settings';

export type CardTextColor = 'dark' | 'light';

type SettingsState = {
  // null keeps the default gradient from styles.scss (--fg-gradient-card-front / --fg-gradient-card-back)
  frontBackground: string | null;
  backBackground: string | null;
  frontText: CardTextColor;
  backText: CardTextColor;
};

const initialState: SettingsState = {
  frontBackground: null,
  backBackground: null,
  frontText: 'dark',
  backText: 'light',
};

// Root-provided so the flashcards and the settings page share the same preferences.
// TODO: persist the preferences on the backend, so they follow the user to other browsers and devices;
//  for now they only live in this browser's local storage
export const SettingsStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),
  withMethods((store) => ({
    setFrontBackground(frontBackground: string | null): void {
      patchState(store, { frontBackground });
    },
    setBackBackground(backBackground: string | null): void {
      patchState(store, { backBackground });
    },
    setFrontText(frontText: CardTextColor): void {
      patchState(store, { frontText });
    },
    setBackText(backText: CardTextColor): void {
      patchState(store, { backText });
    },
    reset(): void {
      patchState(store, initialState);
    },
  })),
  withHooks({
    onInit(store) {
      patchState(store, readPersistedSettings());
      effect(() =>
        writePersistedSettings({
          frontBackground: store.frontBackground(),
          backBackground: store.backBackground(),
          frontText: store.frontText(),
          backText: store.backText(),
        }),
      );
    },
  }),
);

function readPersistedSettings(): Partial<SettingsState> {
  try {
    const raw = localStorage.getItem(SETTINGS_STORAGE_KEY);
    if (!raw) {
      return {};
    }
    const persisted = JSON.parse(raw);
    const settings: Partial<SettingsState> = {};
    // Only keep values of the right shape, so a stale or hand-edited entry can't break the cards
    for (const key of ['frontBackground', 'backBackground'] as const) {
      if (persisted[key] === null || typeof persisted[key] === 'string') {
        settings[key] = persisted[key];
      }
    }
    for (const key of ['frontText', 'backText'] as const) {
      if (persisted[key] === 'dark' || persisted[key] === 'light') {
        settings[key] = persisted[key];
      }
    }
    return settings;
  } catch {
    return {};
  }
}

function writePersistedSettings(settings: SettingsState): void {
  try {
    localStorage.setItem(SETTINGS_STORAGE_KEY, JSON.stringify(settings));
  } catch {
    // Storage unavailable (e.g. private mode): the settings just won't survive a reload
  }
}
