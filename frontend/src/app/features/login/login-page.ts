import {Component, inject, signal} from '@angular/core';
import {form, FormField, required} from '@angular/forms/signals';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {AuthStore} from '../../core/auth/auth.store';
import {LoaderService} from '../../core/loader/loader.service';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-login-page',
  imports: [
    FormField,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    RouterLink,
  ],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss'
})
export class LoginPage {
  protected readonly store = inject(AuthStore);
  protected readonly loader = inject(LoaderService);

  protected readonly credentials = signal({username: '', password: ''});
  protected readonly loginForm = form(this.credentials, (path) => {
    required(path.username, {message: 'Username is required'});
    required(path.password, {message: 'Password is required'});
  });
  protected readonly hidePassword = signal(true);

  protected submit(event: Event): void {
    event.preventDefault();
    if (this.loginForm().invalid() || this.loader.loading()) {
      return;
    }
    this.store.login(this.credentials());
  }
}
