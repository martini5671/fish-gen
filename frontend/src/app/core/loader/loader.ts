import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LoaderService } from './loader.service';

@Component({
  selector: 'app-loader',
  imports: [MatProgressSpinnerModule],
  template: `
    @if (loader.loading()) {
      <div class="loader-overlay" role="progressbar" aria-label="Loading">
        <mat-progress-spinner mode="indeterminate" diameter="56" />
      </div>
    }
  `,
  styles: `
    .loader-overlay {
      position: fixed;
      inset: 0;
      z-index: 1000;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgb(15 23 42 / 0.25);
      backdrop-filter: blur(2px);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Loader {
  protected readonly loader = inject(LoaderService);
}
