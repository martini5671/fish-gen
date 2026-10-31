import {HttpContext, HttpErrorResponse} from '@angular/common/http';
import {computed, inject} from '@angular/core';
import {tapResponse} from '@ngrx/operators';
import {patchState, signalStore, withComputed, withHooks, withMethods, withState,} from '@ngrx/signals';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {ToastrService} from 'ngx-toastr';
import {distinctUntilChanged, EMPTY, exhaustMap, pipe, switchMap, timer} from 'rxjs';
import {GetCollections$Params} from '../api/fn/collections/get-collections';
import {CollectionDto} from '../api/models/collection-dto';
import {CollectionsService} from '../api/services/collections.service';
import {SKIP_LOADER} from '../core/loader/loader.interceptor';

export type CollectionSort = NonNullable<GetCollections$Params['sort']>;

type CollectionsState = {
  collections: CollectionDto[];
  name: string;
  totalElements: number;
  sort: CollectionSort;
  pageIndex: number;
  pageSize: number;
};

const initialState: CollectionsState = {
  collections: [],
  name: '',
  totalElements: 0,
  sort: 'NEWEST',
  pageIndex: 0,
  pageSize: 10,
};

// every second
const POLL_INTERVAL_MS = 1000;

// Provided by CollectionsPage, so every visit starts from the first page with the default sort
export const CollectionsStore = signalStore(
  withState(initialState),
  withComputed(({collections,name, sort, pageIndex, pageSize}) => ({
    query: computed<GetCollections$Params>(() => ({
      // An empty search is left out so the backend returns every collection
      ...(name() ? {name: name()} : {}),
      sort: sort(),
      page: pageIndex(),
      size: pageSize(),
    })),
    pendingCollectionIds: computed(() => {
      return collections()
       .filter(collection => collection.generationStatus === "PENDING")
       .map(pendingCollection => pendingCollection.id);
    })
  })),
  withMethods(
    (store, collectionsService = inject(CollectionsService), toastr = inject(ToastrService)) => ({
      // switchMap drops a slow response when the user has already moved to another page or sort
      loadCollections: rxMethod<GetCollections$Params>(
        pipe(
          switchMap((query) =>
            collectionsService.getCollections(query).pipe(
              tapResponse({
                next: (page) =>
                  patchState(store, {
                    collections: page.content ?? [],
                    totalElements: page.totalElements ?? 0,
                  }),
                error: (error: HttpErrorResponse) =>
                  toastr.error(
                    error.error?.detail ?? 'Could not load your collections, try again later',
                    'Loading failed',
                  ),
              }),
            ),
          ),
        ),
      ),
      pollWhilePending: rxMethod<number[]>(
        pipe(
          // the array is a new reference on every recompute, so compare by content
          distinctUntilChanged((a, b) => a.length === b.length && a.every((id, i) => id === b[i])),
          switchMap((ids) =>
            ids.length === 0
              ? EMPTY
              : timer(POLL_INTERVAL_MS, POLL_INTERVAL_MS).pipe(
                // skip a tick if the previous poll hasn't answered yet
                exhaustMap(() => {
                  console.log('[CollectionsStore] polling collection status for ids', ids);
                  return collectionsService
                    .getCollectionStatus({ids: ids},
                      new HttpContext().set(SKIP_LOADER, true),
                    )
                    .pipe(
                      tapResponse({
                        next: (statuses) => {
                          const statusById = new Map(
                            statuses.map((s) => [s.collectionId, s.generationStatus]),
                          );
                          patchState(store, {
                            // the endpoint only returns statuses, so merge them into the collections we have;
                            // unchanged ones keep their reference so the table doesn't re-render them
                            collections: store.collections().map((c) => {
                              const status = statusById.get(c.id);
                              return status && status !== c.generationStatus
                                ? {...c, generationStatus: status}
                                : c;
                            }),
                          });
                        },
                        // a failed poll is not worth a toast; the next tick tries again
                        error: () => {},
                      }),
                    );
                }),
              ),
          ),
        ),
      ),
      setName(name: string): void {
        // Matches can be fewer than the current page offset, so start from the top
        patchState(store, {name: name.trim(), pageIndex: 0});
      },
      setSort(sort: CollectionSort): void {
        // A new order makes the current page number meaningless, so start from the top
        patchState(store, {sort, pageIndex: 0});
      },
      setPage(pageIndex: number, pageSize: number): void {
        patchState(store, {pageIndex, pageSize});
      },
    }),
  ),
  withMethods((store) => ({
    // For changes made outside the store, e.g. a collection created in the dialog
    reload(): void {
      store.loadCollections(store.query());
    }
  })),
  withHooks({
    onInit(store) {
      // Reloads whenever the query changes
      store.loadCollections(store.query);
      // Polls statuses while any collection on the page is PENDING, stops once none are
      store.pollWhilePending(store.pendingCollectionIds);
    },
  }),
);
