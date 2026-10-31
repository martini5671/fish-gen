import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LoaderService } from './loader.service';

@Component({
  selector: 'app-loader',
  imports: [MatProgressSpinnerModule],
  template: `
    @if (loader.loading()) {
      <div
        class="loader-overlay"
        role="progressbar"
        aria-label="Loading"
        animate.enter="loader-enter"
        animate.leave="loader-leave"
      >
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

    /* The delay keeps quick requests from flashing the overlay; 'both' holds it hidden until then */
    .loader-enter {
      animation: loader-fade-in 250ms ease-out 120ms both;

      mat-progress-spinner {
        animation: loader-pop-in 300ms ease-out 120ms both;
      }
    }

    /* Only a 'to' keyframe, so the fade-out starts from wherever the fade-in got to */
    .loader-leave {
      animation: loader-fade-out 250ms ease-in forwards;
    }

    @keyframes loader-fade-in {
      from {
        opacity: 0;
      }
    }

    @keyframes loader-pop-in {
      from {
        transform: scale(0.85);
      }
    }

    @keyframes loader-fade-out {
      to {
        opacity: 0;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .loader-enter,
      .loader-enter mat-progress-spinner,
      .loader-leave {
        animation-duration: 1ms;
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Loader {
  protected readonly loader = inject(LoaderService);
}
