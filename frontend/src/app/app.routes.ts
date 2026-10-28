import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth/auth.guard';
import { CollectionsPage } from './features/collections/collections-page';
import { HomePage } from './features/home/home-page';
import { LoginPage } from './features/login/login-page';
import { ProfilePage } from './features/profile/profile-page';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    component: LoginPage,
  },
  {
    // HomePage holds the navbar; its children render in the outlet below it
    path: '',
    canActivate: [authGuard],
    component: HomePage,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'collections' },
      { path: 'collections', component: CollectionsPage },
      { path: 'profile', component: ProfilePage },
    ],
  },
  { path: '**', redirectTo: '' },
];
