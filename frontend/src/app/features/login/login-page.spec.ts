import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuthStore } from '../../core/auth/auth.store';
import { LoginPage } from './login-page';

describe('LoginPage', () => {
  let fixture: ComponentFixture<LoginPage>;
  const store = {
    loading: signal(false),
    login: vi.fn(),
  };

  beforeEach(async () => {
    store.login.mockReset();
    await TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [{ provide: AuthStore, useValue: store }],
    }).compileComponents();
    fixture = TestBed.createComponent(LoginPage);
    await fixture.whenStable();
  });

  function type(selector: string, value: string): void {
    const input: HTMLInputElement = fixture.nativeElement.querySelector(selector);
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function submitButton(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('button[type="submit"]');
  }

  it('disables submit while the form is empty', () => {
    expect(submitButton().disabled).toBe(true);
  });

  it('calls login with the entered credentials', async () => {
    type('input[autocomplete="username"]', 'test-user');
    type('input[autocomplete="current-password"]', 'password');
    await fixture.whenStable();

    expect(submitButton().disabled).toBe(false);
    submitButton().click();

    expect(store.login).toHaveBeenCalledWith({ username: 'test-user', password: 'password' });
  });
});
