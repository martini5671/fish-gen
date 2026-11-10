import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { ToastrService } from 'ngx-toastr';
import { provideApiConfiguration } from '../../api/api-configuration';
import { FlashcardDto } from '../../api/models/flashcard-dto';
import { FlashcardsPage } from './flashcards-page';

// Cards numbered from their position in the collection, so card 25 sits on the second page of 20
function cards(from: number, to: number): FlashcardDto[] {
  return Array.from({ length: to - from + 1 }, (_, i) => ({
    id: from + i,
    source: `front ${from + i}`,
    target: `back ${from + i}`,
  }));
}

describe('FlashcardsPage', () => {
  let http: HttpTestingController;
  let harness: RouterTestingHarness;

  async function open(collectionId = 42): Promise<FlashcardsPage> {
    TestBed.configureTestingModule({
      providers: [
        provideRouter(
          [{ path: 'collections/:collectionId', component: FlashcardsPage }],
          withComponentInputBinding(),
        ),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApiConfiguration(''),
        { provide: ToastrService, useValue: { error: vi.fn() } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
    return harness.navigateByUrl(`/collections/${collectionId}`, FlashcardsPage);
  }

  function expectPage(page: number) {
    return http.expectOne(`/api/v1/flashcards/collections/42?page=${page}&size=20`);
  }

  function shownFront(): string | undefined {
    return harness.routeNativeElement?.querySelector('.front')?.textContent?.trim();
  }

  async function click(label: string): Promise<void> {
    harness.routeNativeElement!.querySelector<HTMLButtonElement>(`[aria-label="${label}"]`)!.click();
    await harness.fixture.whenStable();
  }

  afterEach(() => http.verify());

  it('receives the clicked collection from the URL', async () => {
    const page = await open();

    expect(page.collectionId()).toBe(42);
    expectPage(0).flush({ content: [], totalElements: 0 });
  });

  it('wraps around from the first card to the last one on previous', async () => {
    await open();
    expectPage(0).flush({ content: cards(1, 20), totalElements: 25 });
    await harness.fixture.whenStable();
    expect(shownFront()).toBe('front 1');

    await click('Previous flashcard');

    expectPage(1).flush({ content: cards(21, 25), totalElements: 25 });
    await harness.fixture.whenStable();
    expect(shownFront()).toBe('front 25');
  });

  it('goes back to the previous page from the first card of a page', async () => {
    await open();
    expectPage(0).flush({ content: cards(1, 20), totalElements: 25 });
    await harness.fixture.whenStable();
    for (let i = 0; i < 20; i++) {
      await click('Next flashcard');
    }
    expectPage(1).flush({ content: cards(21, 25), totalElements: 25 });
    await harness.fixture.whenStable();
    expect(shownFront()).toBe('front 21');

    await click('Previous flashcard');

    expectPage(0).flush({ content: cards(1, 20), totalElements: 25 });
    await harness.fixture.whenStable();
    expect(shownFront()).toBe('front 20');
  });

  it('shows an empty state for a collection without cards', async () => {
    await open();
    expectPage(0).flush({ content: [], totalElements: 0 });
    await harness.fixture.whenStable();

    expect(harness.routeNativeElement?.textContent).toContain('No flashcards in this collection yet.');
  });
});
