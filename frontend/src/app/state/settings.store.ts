import {HttpErrorResponse} from '@angular/common/http';
import {inject} from '@angular/core';
import {tapResponse} from '@ngrx/operators';
import {getState, patchState, signalStore, withHooks, withMethods, withState} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {ToastrService} from 'ngx-toastr';
import {concatMap, map, pipe, switchMap} from 'rxjs';
import {UserSettingsDto} from '../api/models/user-settings-dto';
import {SettingsService} from '../api/services/settings.service';

export const SETTINGS_STORAGE_KEY = 'fish-gen.settings';

export type CardTextColor = 'dark' | 'light';

export type SettingsState = {
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
// The backend is the source of truth; local storage is only a cache, so the cards get the right colours
// on the first paint after a reload, before loadSettings() answers.
// The setters only change the in-memory state (live preview); call saveSettings() to persist them
export const SettingsStore = signalStore(
  {providedIn: 'root'},
  withState(initialState),
  withMethods((store, settingsService = inject(SettingsService), toastr = inject(ToastrService)) => ({
    setFrontBackground(frontBackground: string | null): void {
      patchState(store, {frontBackground});
    },
    setBackBackground(backBackground: string | null): void {
      patchState(store, {backBackground});
    },
    setFrontText(frontText: CardTextColor): void {
      patchState(store, {frontText});
    },
    setBackText(backText: CardTextColor): void {
      patchState(store, {backText});
    },
    reset(): void {
      patchState(store, initialState);
    },

    // Fetches the logged-in user's settings and caches them in local storage; call it after login
    loadSettings: rxMethod<void>(
      pipe(
        switchMap(() =>
          settingsService.getSettings().pipe(
            tapResponse({
              next: (settings) => {
                patchState(store, toSettingsState(settings));
                writePersistedSettings(toSettingsState(settings));
              },
              error: (error: HttpErrorResponse) =>
                toastr.error(
                  error.error?.detail ?? 'Could not load your settings, try again later',
                  'Loading failed',
                ),
            }),
          ),
        ),
      ),
    ),

    // Sends the given settings (the current state when called without arguments) to the backend,
    // and caches them in local storage once the backend has accepted them.
    // concatMap so quick successive saves reach the backend in order and none is dropped
    saveSettings: rxMethod<SettingsState | void>(
      pipe(
        map((settings) => settings ?? getState(store)),
        concatMap((settings) =>
          settingsService.saveSettings({body: toDto(settings)}).pipe(
            tapResponse({
              next: () => writePersistedSettings(settings),
              error: (error: HttpErrorResponse) =>
                toastr.error(
                  errorDetail(error) ?? 'Could not save your settings, try again later',
                  'Saving failed',
                ),
            }),
          ),
        ),
      ),
    ),
  })),
  withHooks({
    onInit(store) {
      patchState(store, readPersistedSettings());
    },
  }),
);

// saveSettings is generated with responseType 'text', so its error body arrives as an unparsed JSON string
function errorDetail(error: HttpErrorResponse): string | undefined {
  try {
    const body = typeof error.error === 'string' ? JSON.parse(error.error) : error.error;
    return body?.detail;
  } catch {
    return undefined;
  }
}

function toSettingsState(settings: UserSettingsDto): SettingsState {
  return {
    frontBackground: settings.frontBackground ?? null,
    backBackground: settings.backBackground ?? null,
    frontText: settings.frontText === 'dark' ? 'dark': "light",
    backText: settings.backText === 'dark' ? 'dark': "light",
  }
}

function toDto(settings: SettingsState): UserSettingsDto {
  return {
    frontBackground: settings.frontBackground ?? undefined,
    backBackground: settings.backBackground ?? undefined,
    frontText: settings.frontText,
    backText: settings.backText,
  };
}

function readPersistedSettings(): Partial<SettingsState> {
  try {
    const raw = localStorage.getItem(SETTINGS_STORAGE_KEY);
    return raw ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
}

function writePersistedSettings(settings: SettingsState): void {
  try {
    localStorage.setItem(SETTINGS_STORAGE_KEY, JSON.stringify(settings));
  } catch {
    // Storage unavailable (e.g. private mode): the backend still has them, they just load a bit later
  }
}
