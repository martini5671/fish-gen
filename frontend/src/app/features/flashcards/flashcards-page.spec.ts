import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { FlashcardsPage } from './flashcards-page';

describe('FlashcardsPage', () => {
  it('receives the clicked collection from the URL', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter(
          [{ path: 'collections/:collectionId', component: FlashcardsPage }],
          withComponentInputBinding(),
        ),
      ],
    });
    const harness = await RouterTestingHarness.create();

    const page = await harness.navigateByUrl('/collections/42', FlashcardsPage);

    expect(page.collectionId()).toBe(42);
    expect(harness.routeNativeElement?.textContent).toContain('collection #42');
  });
});
