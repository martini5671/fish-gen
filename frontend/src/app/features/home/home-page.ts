import {ChangeDetectionStrategy, Component, computed, inject, OnInit} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthStore } from '../../state/auth.store';
import {TokenUsageStore} from '../../state/token-usage.store';

// Shell for every page behind login: the navbar stays at the top, child routes render below it
@Component({
  selector: 'app-home-page',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatToolbarModule,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
  ],
  templateUrl: './home-page.html',
  styleUrl: './home-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomePage implements OnInit{


  protected readonly authStore = inject(AuthStore);
  protected readonly tokenStore = inject(TokenUsageStore);

  ngOnInit(): void {
    this.tokenStore.loadUsage();
  }

  protected readonly navLinks = [
    { path: '/collections', label: 'Collections' },
    { path: '/profile', label: 'Profile' },
    { path: '/settings', label: 'Settings' },
  ];

  protected readonly initials = computed(() =>
    (this.authStore.username() ?? '').slice(0, 2).toUpperCase(),
  );
}
