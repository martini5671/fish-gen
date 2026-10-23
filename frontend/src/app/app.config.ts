import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { provideRouter } from '@angular/router';
import { provideToastr } from 'ngx-toastr';

import { provideApiConfiguration } from './api/api-configuration';
import { routes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';
import { loaderInterceptor } from './core/loader/loader.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([loaderInterceptor, authInterceptor])),
    // Every form field in the app uses the outlined look defined in styles.scss
    { provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: { appearance: 'outline' } },
    provideToastr({
      positionClass: 'toast-bottom-right',
      timeOut: 5000,
      progressBar: true,
      preventDuplicates: true,
    }),
    // Relative URLs so requests go through the dev server proxy (proxy.conf.json) instead of the
    // http://localhost:8080 server advertised in the OpenAPI spec
    provideApiConfiguration(''),
  ],
};
