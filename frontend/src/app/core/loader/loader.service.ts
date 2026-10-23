import { computed, Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoaderService {
  // A counter rather than a flag, so overlapping requests don't hide the loader too early
  private readonly pending = signal(0);

  readonly loading = computed(() => this.pending() > 0);

  show(): void {
    this.pending.update((count) => count + 1);
  }

  hide(): void {
    this.pending.update((count) => Math.max(0, count - 1));
  }
}
