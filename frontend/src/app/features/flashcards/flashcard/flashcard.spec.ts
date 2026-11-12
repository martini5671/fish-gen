import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideApiConfiguration } from '../../../api/api-configuration';
import { ToastrService } from 'ngx-toastr';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Flashcard } from './flashcard';

describe('Flashcard', () => {
  let component: Flashcard;
  let fixture: ComponentFixture<Flashcard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Flashcard],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApiConfiguration(''),
        { provide: ToastrService, useValue: { error: vi.fn() } },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(Flashcard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
