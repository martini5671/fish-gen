import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MATERIAL_ANIMATIONS } from '@angular/material/core';
import { provideRouter, Router } from '@angular/router';
import { CollectionDto } from '../../api/models/collection-dto';
import { CollectionsPage } from './collections-page';
import { CollectionsStore } from '../../state/collections.store';
import { TokenUsageStore } from '../../state/token-usage.store';

const COLLECTIONS: CollectionDto[] = [
  {
    id: 1,
    name: 'Kitchen vocabulary',
    targetLanguage: 'ENGLISH',
    generationStatus: 'COMPLETED',
    createdAt: '2026-10-12T10:00:00',
  },
  {
    id: 2,
    name: 'Biology – the cell',
    targetLanguage: 'POLISH',
    generationStatus: 'PENDING',
    createdAt: '2026-10-23T10:00:00',
  },
];

describe('CollectionsPage', () => {
  let fixture: ComponentFixture<CollectionsPage>;
  const store = {
    collections: signal<CollectionDto[]>([]),
    totalElements: signal(0),
    name: signal(''),
    sort: signal('NEWEST'),
    pageIndex: signal(0),
    pageSize: signal(10),
    updatedCollectionIds: signal<number[]>([]),
    setName: vi.fn(),
    setSort: vi.fn(),
    setPage: vi.fn(),
    reload: vi.fn(),
    deleteCollection: vi.fn(),
    renameCollection: vi.fn(),
  };
  const tokenUsageStore = { loadUsage: vi.fn() };

  async function render(collections: CollectionDto[]): Promise<HTMLElement> {
    store.collections.set(collections);
    store.totalElements.set(collections.length);
    // The page provides its own store, so the mock replaces it at component level
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: TokenUsageStore, useValue: tokenUsageStore },
        // Dialogs report afterClosed only once their close animation ends, which never fires in jsdom
        { provide: MATERIAL_ANIMATIONS, useValue: { animationsDisabled: true } },
      ],
    });
    TestBed.overrideComponent(CollectionsPage, {
      set: { providers: [{ provide: CollectionsStore, useValue: store }] },
    });
    fixture = TestBed.createComponent(CollectionsPage);
    await fixture.whenStable();
    return fixture.nativeElement;
  }

  beforeEach(() => {
    store.setName.mockReset();
    store.setSort.mockReset();
    store.setPage.mockReset();
    store.deleteCollection.mockReset();
    store.renameCollection.mockReset();
  });

  async function startRenamingFirstRow(page: HTMLElement): Promise<HTMLInputElement> {
    page.querySelector<HTMLButtonElement>('.icon-button.edit')!.click();
    await fixture.whenStable();
    return page.querySelector<HTMLInputElement>('.name-input')!;
  }

  async function typeName(input: HTMLInputElement, name: string): Promise<void> {
    input.value = name;
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  it('renames a collection with the trimmed name when Enter is pressed', async () => {
    const page = await render(COLLECTIONS);
    const input = await startRenamingFirstRow(page);
    expect(input.value).toBe('Kitchen vocabulary');

    await typeName(input, '  Kitchen words ');
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    await fixture.whenStable();

    expect(store.renameCollection).toHaveBeenCalledWith({ id: 1, name: 'Kitchen words' });
    expect(page.querySelector('.name-input')).toBeNull();
  });

  it('does not save an empty name and shows why', async () => {
    const page = await render(COLLECTIONS);
    const input = await startRenamingFirstRow(page);

    await typeName(input, '   ');
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    await fixture.whenStable();

    expect(store.renameCollection).not.toHaveBeenCalled();
    expect(page.querySelector('.name-error')?.textContent).toContain('can not be empty');
    expect(page.querySelector<HTMLButtonElement>('.icon-button.save')!.disabled).toBe(true);
  });

  it('leaves the name unchanged when renaming is cancelled with Escape', async () => {
    const page = await render(COLLECTIONS);
    const input = await startRenamingFirstRow(page);

    await typeName(input, 'Something else');
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    await fixture.whenStable();

    expect(store.renameCollection).not.toHaveBeenCalled();
    expect(page.querySelector('.name-input')).toBeNull();
    expect(page.querySelector('tr.mat-mdc-row')!.textContent).toContain('Kitchen vocabulary');
  });

  // The dialog renders in the CDK overlay, outside the component's element
  async function clickDeleteAndAnswer(page: HTMLElement, answer: 'Delete' | 'Cancel'): Promise<void> {
    page.querySelector<HTMLButtonElement>('.delete-button')!.click();
    await fixture.whenStable();
    const buttons = document.querySelectorAll<HTMLButtonElement>('mat-dialog-actions button');
    [...buttons].find((button) => button.textContent?.trim() === answer)!.click();
    await fixture.whenStable();
  }

  it('deletes a collection only after the warning is confirmed', async () => {
    const page = await render(COLLECTIONS);

    await clickDeleteAndAnswer(page, 'Delete');

    expect(store.deleteCollection).toHaveBeenCalledWith(1);
  });

  it('keeps the collection when the warning is cancelled', async () => {
    const page = await render(COLLECTIONS);

    await clickDeleteAndAnswer(page, 'Cancel');

    expect(store.deleteCollection).not.toHaveBeenCalled();
  });

  it('shows one row per collection with readable language and status', async () => {
    const page = await render(COLLECTIONS);

    const rows = page.querySelectorAll('tr.mat-mdc-row');
    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('Kitchen vocabulary');
    expect(rows[0].textContent).toContain('English');
    expect(rows[0].textContent).toContain('Completed');
    expect(rows[1].textContent).toContain('Pending');
  });

  it('opens the flashcards of a collection when its row is clicked', async () => {
    const page = await render(COLLECTIONS);
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    page.querySelectorAll<HTMLElement>('tr.mat-mdc-row')[1].click();

    expect(navigate).toHaveBeenCalledWith(['/collections', 2]);
  });

  it('does not open the collection when its delete button is clicked', async () => {
    const page = await render(COLLECTIONS);
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    await clickDeleteAndAnswer(page, 'Cancel');

    expect(navigate).not.toHaveBeenCalled();
  });

  it('shows an empty message when there are no collections', async () => {
    const page = await render([]);

    expect(page.querySelector('.empty')?.textContent).toContain("You don't have any collections yet.");
  });

  it('searches by the typed name once typing pauses', async () => {
    const page = await render(COLLECTIONS);
    // Fake timers only after render, so whenStable is not stuck waiting on them
    vi.useFakeTimers();
    try {
      const input = page.querySelector<HTMLInputElement>('input[type="search"]')!;

      input.value = 'kit';
      input.dispatchEvent(new Event('input'));
      input.value = 'kitchen ';
      input.dispatchEvent(new Event('input'));
      expect(store.setName).not.toHaveBeenCalled();

      vi.advanceTimersByTime(300);

      expect(store.setName).toHaveBeenCalledTimes(1);
      expect(store.setName).toHaveBeenCalledWith('kitchen');
    } finally {
      vi.useRealTimers();
    }
  });

  it('asks the store for the next page when the paginator moves', async () => {
    const page = await render(COLLECTIONS);
    store.totalElements.set(30);
    await fixture.whenStable();

    page.querySelector<HTMLButtonElement>('.mat-mdc-paginator-navigation-next')!.click();

    expect(store.setPage).toHaveBeenCalledWith(1, 10);
  });
});
