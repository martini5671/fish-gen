import {computed, DestroyRef, inject, Injectable, signal} from '@angular/core';

@Injectable({providedIn: 'root'})
export class LoaderService {
  // wait this long before showing the spinner, so fast requests don't flash it
  private readonly SHOW_SPINNER_THRESHOLD = 400;
  // once shown, keep the spinner up at least this long, so it doesn't blink on and off
  private readonly MIN_SPINNER_DURATION = 300;

  // A counter rather than a flag, so overlapping requests don't hide the loader too early
  private readonly pending = signal(0);
  private readonly visible = signal(false);
  private shownAt = 0;
  private showTimeout?: ReturnType<typeof setTimeout>;
  private hideTimeout?: ReturnType<typeof setTimeout>;

  // true while any request is in flight; use it to block double submits
  readonly busy = computed(() => this.pending() > 0);
  // decides if the spinner shows on the screen
  readonly loading = this.visible.asReadonly();

  constructor() {
    inject(DestroyRef).onDestroy(() => this.clearTimeouts());
  }

  show(): void {
    if (this.pending() === 0) {
      // a new request came in while the spinner was waiting out its minimum duration, so keep it up
      clearTimeout(this.hideTimeout);
      if (!this.visible()) {
        this.showTimeout = setTimeout(() => {
          this.shownAt = Date.now();
          this.visible.set(true);
        }, this.SHOW_SPINNER_THRESHOLD);
      }
    }
    this.pending.update((count) => count + 1);
  }

  hide(): void {
    this.pending.update((count) => Math.max(0, count - 1));
    if (this.pending() > 0) return;

    clearTimeout(this.showTimeout);
    if (this.visible()) {
      const remaining = this.MIN_SPINNER_DURATION - (Date.now() - this.shownAt);
      if (remaining <= 0) {
        this.visible.set(false);
      } else {
        this.hideTimeout = setTimeout(() => this.visible.set(false), remaining);
      }
    }
  }

  private clearTimeouts(): void {
    clearTimeout(this.showTimeout);
    clearTimeout(this.hideTimeout);
  }
}
