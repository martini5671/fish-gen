import { ChangeDetectionStrategy, Component } from '@angular/core';

// Placeholder for the user's profile
@Component({
  selector: 'app-profile-page',
  template: `<h1 class="title">Profile</h1>`,
  styles: `
    .title {
      margin: 0;
      font-family: 'Plus Jakarta Sans', Inter, sans-serif;
      font-size: 26px;
      font-weight: 700;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfilePage {}
