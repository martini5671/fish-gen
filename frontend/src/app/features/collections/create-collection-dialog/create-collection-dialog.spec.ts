import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { of, throwError } from 'rxjs';
import { CollectionsService } from '../../../api/services/collections.service';
import { CreateCollectionDialog } from './create-collection-dialog';

describe('CreateCollectionDialog', () => {
  let fixture: ComponentFixture<CreateCollectionDialog>;
  const dialogRef = { close: vi.fn() };
  const collectionsService = { createCollection: vi.fn() };
  const toastr = { success: vi.fn(), error: vi.fn() };

  async function render(): Promise<HTMLElement> {
    TestBed.configureTestingModule({
      providers: [
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: CollectionsService, useValue: collectionsService },
        { provide: ToastrService, useValue: toastr },
      ],
    });
    fixture = TestBed.createComponent(CreateCollectionDialog);
    await fixture.whenStable();
    return fixture.nativeElement;
  }

  async function type(element: HTMLInputElement, value: string) {
    element.value = value;
    element.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  async function submit(page: HTMLElement) {
    page.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    await fixture.whenStable();
  }

  beforeEach(() => vi.clearAllMocks());

  it('only asks for the collection name', async () => {
    const page = await render();

    expect(page.querySelectorAll('input')).toHaveLength(1);
    expect(page.querySelector('textarea')).toBeNull();
    expect(page.querySelector('mat-select')).toBeNull();
  });

  it('sends the trimmed name and closes as created', async () => {
    collectionsService.createCollection.mockReturnValue(of(undefined));
    const page = await render();
    await type(page.querySelector('input')!, '  Spanish verbs  ');

    await submit(page);

    expect(collectionsService.createCollection).toHaveBeenCalledWith({
      body: { name: 'Spanish verbs' },
    });
    expect(dialogRef.close).toHaveBeenCalledWith(true);
  });

  it('sends no name when left blank so the backend generates one', async () => {
    collectionsService.createCollection.mockReturnValue(of(undefined));
    const page = await render();
    await type(page.querySelector('input')!, '   ');

    await submit(page);

    expect(collectionsService.createCollection).toHaveBeenCalledWith({ body: { name: null } });
    expect(dialogRef.close).toHaveBeenCalledWith(true);
  });

  it('does not send a name longer than 255 characters', async () => {
    const page = await render();
    await type(page.querySelector('input')!, 'a'.repeat(256));

    await submit(page);

    expect(collectionsService.createCollection).not.toHaveBeenCalled();
    expect(page.querySelector('mat-error')?.textContent).toContain('at most 255 characters');
  });

  it('stays open and shows the backend error when creation fails', async () => {
    collectionsService.createCollection.mockReturnValue(
      throwError(
        () => new HttpErrorResponse({ status: 400, error: { detail: 'Invalid collection name' } }),
      ),
    );
    const page = await render();

    await submit(page);

    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(toastr.error).toHaveBeenCalledWith('Invalid collection name', 'Creation failed');
    expect(page.querySelector<HTMLButtonElement>('button[type="submit"]')!.disabled).toBe(false);
  });
});
