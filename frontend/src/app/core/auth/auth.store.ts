import { HttpErrorResponse } from '@angular/common/http';
import { computed, effect, inject } from '@angular/core';
import { Router } from '@angular/router';
import { tapResponse } from '@ngrx/operators';
import {
  patchState,
  signalStore,
  withComputed,
  withHooks,
  withMethods,
  withState,
} from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { ToastrService } from 'ngx-toastr';
import { pipe, switchMap } from 'rxjs';
import { LoginDto } from '../../api/models/login-dto';
import { TokenDto } from '../../api/models/token-dto';
import { AuthService } from '../../api/services/auth.service';

export const AUTH_STORAGE_KEY = 'fish-gen.auth';

type AuthState = {
  token: string | null;
  expiresAt: number | null;
  username: string | null;
};

type PersistedAuth = AuthState;

const initialState: AuthState = {
  token: null,
  expiresAt: null,
  username: null,
};

export const AuthStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),
  withComputed(({ token, expiresAt }) => ({
    // Not reactive to the clock; an expired token is caught by the 401 handling in authInterceptor
    isAuthenticated: computed(() => !!token() && !!expiresAt() && expiresAt()! > Date.now()),
  })),
  withMethods(
    (
      store,
      authService = inject(AuthService),
      router = inject(Router),
      toastr = inject(ToastrService),
    ) => ({
      login: rxMethod<LoginDto>(
        pipe(
          switchMap((credentials) =>
            authService.login({ body: credentials }).pipe(
              tapResponse({
                next: (tokenDto) => {
                  patchState(store, fromToken(tokenDto));
                  void router.navigateByUrl('/');
                },
                error: (error: HttpErrorResponse) => {
                  // error.error is the backend's ProblemDetail body
                  toastr.error(
                    error.error?.detail ?? 'Login failed, try again later',
                    'Login failed',
                  );
                },
              }),
            ),
          ),
        ),
      ),
      logout(): void {
        patchState(store, initialState);
        void router.navigateByUrl('/login');
      },
    }),
  ),
  withHooks({
    onInit(store) {
      const persisted = readPersistedAuth();
      if (persisted) {
        patchState(store, persisted);
      }
      effect(() => {
        const token = store.token();
        writePersistedAuth(
          token ? { token, expiresAt: store.expiresAt(), username: store.username() } : null,
        );
      });
    },
  }),
);

function fromToken({ accessToken, expiresAt, username }: TokenDto): PersistedAuth {
  return { token: accessToken, expiresAt, username };
}

function readPersistedAuth(): PersistedAuth | null {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) {
      return null;
    }
    const persisted: PersistedAuth = JSON.parse(raw);
    if (!persisted.token || !persisted.expiresAt || persisted.expiresAt <= Date.now()) {
      localStorage.removeItem(AUTH_STORAGE_KEY);
      return null;
    }
    return persisted;
  } catch {
    return null;
  }
}

function writePersistedAuth(auth: PersistedAuth | null): void {
  try {
    if (auth) {
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth));
    } else {
      localStorage.removeItem(AUTH_STORAGE_KEY);
    }
  } catch {
    // Storage unavailable (e.g. private mode): the session just won't survive a reload
  }
}
