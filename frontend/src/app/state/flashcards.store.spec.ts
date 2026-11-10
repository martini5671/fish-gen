import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ToastrService } from 'ngx-toastr';
import { provideApiConfiguration } from '../api/api-configuration';
import { FlashcardDto } from '../api/models/flashcard-dto';
import { FlashcardsStore } from './flashcards.store';

const FLASHCARDS: FlashcardDto[] = [
  { id: 1, source: 'fox', target: 'lis' },
  { id: 2, source: 'owl', target: 'sowa' },
];

describe('FlashcardsStore', () => {
  const toastr = { error: vi.fn() };
  let http: HttpTestingController;

  function setup(): InstanceType<typeof FlashcardsStore> {
    TestBed.configureTestingModule({
      providers: [
        FlashcardsStore,
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApiConfiguration(''),
        { provide: ToastrService, useValue: toastr },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    const store = TestBed.inject(FlashcardsStore);
    TestBed.tick();
    return store;
  }

  function expectRequest(collectionId: number, query: string) {
    return http.expectOne(`/api/v1/flashcards/collections/${collectionId}?${query}`);
  }

  beforeEach(() => toastr.error.mockReset());
  afterEach(() => http.verify());

  it('requests nothing until a collection is set', () => {
    setup();

    http.expectNone(() => true);
  });

  it('loads the first page of the chosen collection', () => {
    const store = setup();

    store.setCollection(7);
    TestBed.tick();

    expectRequest(7, 'page=0&size=20').flush({ content: FLASHCARDS, totalElements: 2 });
    expect(store.flashcards()).toEqual(FLASHCARDS);
    expect(store.totalElements()).toBe(2);
  });

  it('requests the chosen page and page size', () => {
    const store = setup();
    store.setCollection(7);
    TestBed.tick();
    expectRequest(7, 'page=0&size=20').flush({ content: FLASHCARDS, totalElements: 30 });

    store.setPage(1, 10);
    TestBed.tick();

    expectRequest(7, 'page=1&size=10').flush({ content: [], totalElements: 30 });
  });

  it('drops the old page cards while the new page loads', () => {
    const store = setup();
    store.setCollection(7);
    TestBed.tick();
    expectRequest(7, 'page=0&size=20').flush({ content: FLASHCARDS, totalElements: 30 });

    store.setPage(1, 20);
    TestBed.tick();

    expect(store.flashcards()).toEqual([]);
    expect(store.totalElements()).toBe(30);
    expectRequest(7, 'page=1&size=20').flush({ content: FLASHCARDS, totalElements: 30 });
  });

  it('is loaded only once the first response arrives', () => {
    const store = setup();
    store.setCollection(7);
    TestBed.tick();

    expect(store.loaded()).toBe(false);
    expectRequest(7, 'page=0&size=20').flush({ content: [], totalElements: 0 });
    expect(store.loaded()).toBe(true);
  });

  it('starts from the first page when another collection is set', () => {
    const store = setup();
    store.setCollection(7);
    TestBed.tick();
    expectRequest(7, 'page=0&size=20').flush({ content: FLASHCARDS, totalElements: 30 });
    store.setPage(1, 20);
    TestBed.tick();
    expectRequest(7, 'page=1&size=20').flush({ content: [], totalElements: 30 });

    store.setCollection(8);
    TestBed.tick();

    expectRequest(8, 'page=0&size=20').flush({ content: [], totalElements: 0 });
    expect(store.pageIndex()).toBe(0);
  });

  it('has no pages to move to before anything is loaded', () => {
    const store = setup();

    expect(store.totalPages()).toBe(0);
    expect(store.hasPreviousPage()).toBe(false);
    expect(store.hasNextPage()).toBe(false);
  });

  it('knows which neighbouring pages exist', () => {
    const store = setup();
    store.setCollection(7);
    TestBed.tick();
    // 45 cards in pages of 20: pages 0, 1 and 2, the last one holding 5
    expectRequest(7, 'page=0&size=20').flush({ content: FLASHCARDS, totalElements: 45 });

    expect(store.totalPages()).toBe(3);
    expect(store.hasPreviousPage()).toBe(false);
    expect(store.hasNextPage()).toBe(true);

    store.setPage(1, 20);
    TestBed.tick();
    expectRequest(7, 'page=1&size=20').flush({ content: FLASHCARDS, totalElements: 45 });
    expect(store.hasPreviousPage()).toBe(true);
    expect(store.hasNextPage()).toBe(true);

    store.setPage(2, 20);
    TestBed.tick();
    expectRequest(7, 'page=2&size=20').flush({ content: FLASHCARDS, totalElements: 45 });
    expect(store.hasPreviousPage()).toBe(true);
    expect(store.hasNextPage()).toBe(false);
  });

  it('has no next page when the cards exactly fill the last page', () => {
    const store = setup();
    store.setCollection(7);
    TestBed.tick();

    expectRequest(7, 'page=0&size=20').flush({ content: FLASHCARDS, totalElements: 20 });

    expect(store.totalPages()).toBe(1);
    expect(store.hasNextPage()).toBe(false);
  });

  it('shows a toast when loading fails', () => {
    const store = setup();
    store.setCollection(7);
    TestBed.tick();

    expectRequest(7, 'page=0&size=20').flush(
      { title: 'Not found', detail: 'Collection with id 7 not found' },
      { status: 404, statusText: 'Not Found' },
    );

    expect(toastr.error).toHaveBeenCalledWith('Collection with id 7 not found', 'Loading failed');
    expect(store.flashcards()).toEqual([]);
  });
});
