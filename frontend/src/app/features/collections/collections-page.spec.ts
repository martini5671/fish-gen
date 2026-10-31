import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
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
    setName: vi.fn(),
    setSort: vi.fn(),
    setPage: vi.fn(),
    reload: vi.fn(),
  };
  const tokenUsageStore = { loadUsage: vi.fn() };

  async function render(collections: CollectionDto[]): Promise<HTMLElement> {
    store.collections.set(collections);
    store.totalElements.set(collections.length);
    // The page provides its own store, so the mock replaces it at component level
    TestBed.configureTestingModule({
      providers: [{ provide: TokenUsageStore, useValue: tokenUsageStore }],
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
