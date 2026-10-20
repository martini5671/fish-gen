import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../../api/services/auth.service';
import { AuthStore } from './auth.store';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(AuthStore);
  if (req.url.endsWith(AuthService.LoginPath)) {
    return next(req);
  }

  const token = store.token();
  const authorized = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(authorized).pipe(
    catchError((error: unknown) => {
      // Expired or revoked token: drop the session and send the user back to the login page
      if (error instanceof HttpErrorResponse && error.status === 401 && token) {
        store.logout();
      }
      return throwError(() => error);
    }),
  );
};
