import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CollectionDto } from '../../api/models/collection-dto';
import { CollectionsPage } from './collections-page';
import { CollectionsStore } from './collections.store';

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
    sort: signal('NEWEST'),
    pageIndex: signal(0),
    pageSize: signal(10),
    setSort: vi.fn(),
    setPage: vi.fn(),
  };

  async function render(collections: CollectionDto[]): Promise<HTMLElement> {
    store.collections.set(collections);
    store.totalElements.set(collections.length);
    // The page provides its own store, so the mock replaces it at component level
    TestBed.overrideComponent(CollectionsPage, {
      set: { providers: [{ provide: CollectionsStore, useValue: store }] },
    });
    fixture = TestBed.createComponent(CollectionsPage);
    await fixture.whenStable();
    return fixture.nativeElement;
  }

  beforeEach(() => {
    store.setSort.mockReset();
    store.setPage.mockReset();
  });

  it('shows one row per collection with readable language and status', async () => {
    const page = await render(COLLECTIONS);

    const rows = page.querySelectorAll('tr.mat-mdc-row');
    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('Kitchen vocabulary');
    expect(rows[0].textContent).toContain('English');
    expect(rows[0].textContent).toContain('Ready');
    expect(rows[1].textContent).toContain('Generating');
  });

  it('shows an empty message when there are no collections', async () => {
    const page = await render([]);

    expect(page.querySelector('.empty')?.textContent).toContain("You don't have any collections yet.");
  });

  it('asks the store for the next page when the paginator moves', async () => {
    const page = await render(COLLECTIONS);
    store.totalElements.set(30);
    await fixture.whenStable();

    page.querySelector<HTMLButtonElement>('.mat-mdc-paginator-navigation-next')!.click();

    expect(store.setPage).toHaveBeenCalledWith(1, 10);
  });
});
