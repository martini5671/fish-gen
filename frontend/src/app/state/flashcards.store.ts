import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject } from '@angular/core';
import { tapResponse } from '@ngrx/operators';
import { patchState, signalStore, withComputed, withHooks, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { ToastrService } from 'ngx-toastr';
import { filter, pipe, switchMap } from 'rxjs';
import { GetByCollectionId$Params } from '../api/fn/flashcards/get-by-collection-id';
import { FlashcardDto } from '../api/models/flashcard-dto';
import { FlashcardsService } from '../api/services/flashcards.service';

type FlashcardsState = {
  collectionId: number | null;
  flashcards: FlashcardDto[];
  totalElements: number;
  pageIndex: number;
  pageSize: number;
  // false until the first response for the collection arrives, so the page can tell 'loading' from 'empty'
  loaded: boolean;
};

const initialState: FlashcardsState = {
  collectionId: null,
  flashcards: [],
  totalElements: 0,
  pageIndex: 0,
  pageSize: 20,
  loaded: false,
};

// Provided by FlashcardsPage, so every visit starts empty on the first page.
// The backend always returns the cards sorted by id, in the order they were generated
export const FlashcardsStore = signalStore(
  withState(initialState),
  withComputed(({ collectionId, pageIndex, pageSize, totalElements }) => ({
    totalPages: computed(() => Math.ceil(totalElements() / pageSize())),
    hasPreviousPage: computed(() => pageIndex() > 0),
    hasNextPage: computed(() => (pageIndex() + 1) * pageSize() < totalElements()),
    // null until a collection is set, so nothing is requested before that
    query: computed<GetByCollectionId$Params | null>(() => {
      const id = collectionId();
      return id === null ? null : { collectionId: id, page: pageIndex(), size: pageSize() };
    })
  })),
  withMethods(
    (store, flashcardsService = inject(FlashcardsService), toastr = inject(ToastrService)) => ({
      // switchMap drops a slow response when the user has already moved to another page or collection
      loadFlashcards: rxMethod<GetByCollectionId$Params | null>(
        pipe(
          filter((query) => query !== null),
          switchMap((query) =>
            flashcardsService.getByCollectionId(query).pipe(
              tapResponse({
                next: (page) =>
                  patchState(store, {
                    flashcards: page.content ?? [],
                    totalElements: page.totalElements ?? 0,
                    loaded: true,
                  }),
                error: (error: HttpErrorResponse) => {
                  patchState(store, { loaded: true });
                  toastr.error(
                    error.error?.detail ?? 'Could not load the flashcards, try again later',
                    'Loading failed',
                  );
                },
              }),
            ),
          ),
        ),
      ),
      setCollection(collectionId: number): void {
        // Another collection's cards and page number don't apply, so start over
        patchState(store, { ...initialState, collectionId });
      },
      setPage(pageIndex: number, pageSize: number): void {
        // The old page's cards are dropped, so they're never shown as if they belonged to the new page
        patchState(store, { pageIndex, pageSize, flashcards: [] });
      },
    }),
  ),
  withHooks({
    onInit(store) {
      // Reloads whenever the collection or the page changes
      store.loadFlashcards(store.query);
    },
  }),
);
