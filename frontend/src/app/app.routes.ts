import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth/auth.guard';
import { HomePage } from './features/home/home-page';
import { LoginPage } from './features/login/login-page';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    component: LoginPage,
  },
  {
    path: '',
    pathMatch: 'full',
    canActivate: [authGuard],
    component: HomePage,
  },
  { path: '**', redirectTo: '' },
];
