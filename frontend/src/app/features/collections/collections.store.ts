import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject } from '@angular/core';
import { tapResponse } from '@ngrx/operators';
import {
  patchState,
  signalStore,
  withComputed,
  withHooks,
  withMethods,
  withState,
} from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { ToastrService } from 'ngx-toastr';
import { pipe, switchMap } from 'rxjs';
import { GetCollections$Params } from '../../api/fn/collections/get-collections';
import { CollectionDto } from '../../api/models/collection-dto';
import { CollectionsService } from '../../api/services/collections.service';

export type CollectionSort = NonNullable<GetCollections$Params['sort']>;

type CollectionsState = {
  collections: CollectionDto[];
  totalElements: number;
  sort: CollectionSort;
  pageIndex: number;
  pageSize: number;
};

const initialState: CollectionsState = {
  collections: [],
  totalElements: 0,
  sort: 'NEWEST',
  pageIndex: 0,
  pageSize: 10,
};

// Provided by CollectionsPage, so every visit starts from the first page with the default sort
export const CollectionsStore = signalStore(
  withState(initialState),
  withComputed(({ sort, pageIndex, pageSize }) => ({
    query: computed<GetCollections$Params>(() => ({
      sort: sort(),
      page: pageIndex(),
      size: pageSize(),
    })),
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
      setSort(sort: CollectionSort): void {
        // A new order makes the current page number meaningless, so start from the top
        patchState(store, { sort, pageIndex: 0 });
      },
      setPage(pageIndex: number, pageSize: number): void {
        patchState(store, { pageIndex, pageSize });
      },
    }),
  ),
  withHooks({
    onInit(store) {
      // Reloads whenever the query changes
      store.loadCollections(store.query);
    },
  }),
);
