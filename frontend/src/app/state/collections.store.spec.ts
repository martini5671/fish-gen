import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ToastrService } from 'ngx-toastr';
import { provideApiConfiguration } from '../api/api-configuration';
import { CollectionDto } from '../api/models/collection-dto';
import { CollectionsStore } from './collections.store';

const COLLECTION: CollectionDto = {
  id: 1,
  name: 'Kitchen vocabulary',
  targetLanguage: 'ENGLISH',
  generationStatus: 'COMPLETED',
  createdAt: '2026-10-12T10:00:00',
};

describe('CollectionsStore', () => {
  const toastr = { error: vi.fn() };
  let http: HttpTestingController;

  function setup(): InstanceType<typeof CollectionsStore> {
    TestBed.configureTestingModule({
      providers: [
        CollectionsStore,
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApiConfiguration(''),
        { provide: ToastrService, useValue: toastr },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    const store = TestBed.inject(CollectionsStore);
    TestBed.tick();
    return store;
  }

  function expectQuery(query: string) {
    return http.expectOne(`/api/v1/collections?${query}`);
  }

  beforeEach(() => toastr.error.mockReset());
  afterEach(() => http.verify());

  it('loads the first page of the newest collections on init', () => {
    const store = setup();

    expectQuery('sort=NEWEST&page=0&size=10').flush({ content: [COLLECTION], totalElements: 1 });

    expect(store.collections()).toEqual([COLLECTION]);
    expect(store.totalElements()).toBe(1);
  });

  it('reloads from the first page when the sort changes', () => {
    const store = setup();
    expectQuery('sort=NEWEST&page=0&size=10').flush({ content: [], totalElements: 30 });
    store.setPage(2, 10);
    TestBed.tick();
    expectQuery('sort=NEWEST&page=2&size=10').flush({ content: [], totalElements: 30 });

    store.setSort('NAME_ASC');
    TestBed.tick();

    expectQuery('sort=NAME_ASC&page=0&size=10').flush({ content: [COLLECTION], totalElements: 30 });
    expect(store.pageIndex()).toBe(0);
    expect(store.collections()).toEqual([COLLECTION]);
  });

  it('searches by name from the first page and drops an empty search', () => {
    const store = setup();
    expectQuery('sort=NEWEST&page=0&size=10').flush({ content: [], totalElements: 30 });
    store.setPage(2, 10);
    TestBed.tick();
    expectQuery('sort=NEWEST&page=2&size=10').flush({ content: [], totalElements: 30 });

    store.setName('  kitchen ');
    TestBed.tick();
    expectQuery('name=kitchen&sort=NEWEST&page=0&size=10').flush({
      content: [COLLECTION],
      totalElements: 1,
    });
    expect(store.collections()).toEqual([COLLECTION]);

    store.setName('');
    TestBed.tick();
    expectQuery('sort=NEWEST&page=0&size=10').flush({ content: [], totalElements: 30 });
  });

  it('requests the chosen page and page size', () => {
    const store = setup();
    expectQuery('sort=NEWEST&page=0&size=10').flush({ content: [], totalElements: 0 });

    store.setPage(1, 25);
    TestBed.tick();

    expectQuery('sort=NEWEST&page=1&size=25').flush({ content: [], totalElements: 0 });
  });

  it('shows a toast and keeps the current rows when loading fails', () => {
    setup();

    expectQuery('sort=NEWEST&page=0&size=10').flush(
      { title: 'Server error', detail: 'Something broke' },
      { status: 500, statusText: 'Internal Server Error' },
    );

    expect(toastr.error).toHaveBeenCalledWith('Something broke', 'Loading failed');
  });
});
