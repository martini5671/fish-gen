import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthStore } from '../../core/auth/auth.store';

// Placeholder landing page; collections will live here
@Component({
  selector: 'app-home-page',
  imports: [MatButtonModule, MatToolbarModule],
  template: `
    <mat-toolbar>
      <span>Fish-gen</span>
      <span class="spacer"></span>
      <span class="user">Logged in as {{ store.username() }}</span>
      <button mat-button (click)="store.logout()">Log out</button>
    </mat-toolbar>
  `,
  styles: `
    .spacer {
      flex: 1;
    }
    .user {
      font: var(--mat-sys-body-medium);
      margin-inline-end: 8px;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomePage {
  protected readonly store = inject(AuthStore);
}
