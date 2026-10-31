import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { tapResponse } from '@ngrx/operators';
import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { ToastrService } from 'ngx-toastr';
import { pipe, switchMap } from 'rxjs';
import { UsageService } from '../api/services/usage.service';

type TokenUsageState = {
  aiCredits: number | null;
};

const initialState: TokenUsageState = {
  aiCredits: null,
};

// Root-provided so the remaining credits are shared app-wide; call loadUsage() after login
// and again after anything that spends credits, e.g. generating flashcards
export const TokenUsageStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),
  withMethods((store, usageService = inject(UsageService), toastr = inject(ToastrService)) => ({
    loadUsage: rxMethod<void>(
      pipe(
        switchMap(() =>
          usageService.getTokenUsage().pipe(
            tapResponse({
              next: (usage) => patchState(store, { aiCredits: usage.aiCredits ?? 0 }),
              error: (error: HttpErrorResponse) =>
                toastr.error(
                  error.error?.detail ?? 'Could not load your AI credits, try again later',
                  'Loading failed',
                ),
            }),
          ),
        ),
      ),
    ),
    reset(): void {
      patchState(store, initialState);
    },
  })),
);
