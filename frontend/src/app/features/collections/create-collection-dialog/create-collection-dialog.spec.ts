import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { of, throwError } from 'rxjs';
import { FlashcardsService } from '../../../api/services/flashcards.service';
import { CreateCollectionDialog } from './create-collection-dialog';

describe('CreateCollectionDialog', () => {
  let fixture: ComponentFixture<CreateCollectionDialog>;
  const dialogRef = { close: vi.fn() };
  const flashcardsService = { createAiFlashcards: vi.fn() };
  const toastr = { success: vi.fn(), error: vi.fn() };

  async function render(): Promise<HTMLElement> {
    TestBed.configureTestingModule({
      providers: [
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: FlashcardsService, useValue: flashcardsService },
        { provide: ToastrService, useValue: toastr },
      ],
    });
    fixture = TestBed.createComponent(CreateCollectionDialog);
    await fixture.whenStable();
    return fixture.nativeElement;
  }

  async function type(element: HTMLInputElement | HTMLTextAreaElement, value: string) {
    element.value = value;
    element.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  async function submit(page: HTMLElement) {
    page.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    await fixture.whenStable();
  }

  beforeEach(() => vi.clearAllMocks());

  it('does not send a text shorter than 10 characters', async () => {
    const page = await render();
    await type(page.querySelector('textarea')!, 'too short');

    await submit(page);

    expect(flashcardsService.createAiFlashcards).not.toHaveBeenCalled();
    expect(page.querySelector('mat-error')?.textContent).toContain('at least 10 characters');
  });

  it('counts the typed characters', async () => {
    const page = await render();

    await type(page.querySelector('textarea')!, 'Hello world');

    expect(page.textContent).toContain('11 / 3000');
  });

  it('sends the text in English with no name by default and closes as created', async () => {
    flashcardsService.createAiFlashcards.mockReturnValue(of(undefined));
    const page = await render();
    await type(page.querySelector('textarea')!, 'Der Hund schläft im Garten.');
    await type(page.querySelector('input')!, '   ');

    await submit(page);

    expect(flashcardsService.createAiFlashcards).toHaveBeenCalledWith({
      body: {
        textToScan: 'Der Hund schläft im Garten.',
        collectionName: null,
        targetLanguage: 'ENGLISH',
      },
    });
    expect(dialogRef.close).toHaveBeenCalledWith(true);
  });

  it('stays open and shows the backend error when creation fails', async () => {
    flashcardsService.createAiFlashcards.mockReturnValue(
      throwError(
        () => new HttpErrorResponse({ status: 402, error: { detail: 'Not enough AI credits' } }),
      ),
    );
    const page = await render();
    await type(page.querySelector('textarea')!, 'Der Hund schläft im Garten.');

    await submit(page);

    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(toastr.error).toHaveBeenCalledWith('Not enough AI credits', 'Creation failed');
    expect(page.querySelector<HTMLButtonElement>('button[type="submit"]')!.disabled).toBe(false);
  });
});
