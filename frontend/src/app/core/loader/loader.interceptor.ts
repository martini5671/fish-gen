import { HttpContextToken, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';
import { LoaderService } from './loader.service';

/** Set to true on a request's context to keep it from showing the global loader (e.g. polling). */
export const SKIP_LOADER = new HttpContextToken<boolean>(() => false);

export const loaderInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.context.get(SKIP_LOADER)) {
    return next(req);
  }

  const loader = inject(LoaderService);
  loader.show();
  // finalize covers success, error and cancellation (unsubscribe)
  return next(req).pipe(finalize(() => loader.hide()));
};
