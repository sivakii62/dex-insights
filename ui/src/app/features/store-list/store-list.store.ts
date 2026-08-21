import { computed, inject } from '@angular/core';
import { patchState, signalStore, withComputed, withHooks, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { tapResponse } from '@ngrx/operators';
import { pipe, switchMap } from 'rxjs';

import { StoreApiService } from '../../core/api/store-api.service';
import { toErrorMessage } from '../../core/api/to-error-message';
import type { Page } from '../../core/models/page.model';
import type { Store, StoreQuery, StoreSortField, StoreStatus, SortDirection } from '../../core/models/store.model';

const PAGE_SIZE = 10;

const emptyPage: Page<Store> = {
  content: [],
  page: 0,
  size: PAGE_SIZE,
  totalElements: 0,
  totalPages: 0,
  hasNext: false,
};

interface StoreListState {
  brand: string;
  status: StoreStatus | '';
  sortBy: StoreSortField;
  direction: SortDirection;
  pageIndex: number;
  page: Page<Store>;
  loading: boolean;
  error: string | null;
}

const initialState: StoreListState = {
  brand: '',
  status: '',
  sortBy: 'STORE_ID',
  direction: 'ASC',
  pageIndex: 0,
  page: emptyPage,
  loading: true,
  error: null,
};

/**
 * Owns the store list's filter/sort/page state and keeps it in sync with GET /v1/stores.
 *
 * Provided per-route (see StoreListComponent's `providers`) rather than in root, so a fresh filter
 * state starts each time the list is navigated to instead of leaking between visits.
 */
export const StoreListStore = signalStore(
  withState(initialState),
  withComputed(({ brand, status, sortBy, direction, pageIndex }) => ({
    /** Recomputes whenever a filter/sort/page field changes; loadStores below reacts to it. */
    query: computed<StoreQuery>(() => ({
      brand: brand().trim() || undefined,
      status: status() || undefined,
      sortBy: sortBy(),
      direction: direction(),
      page: pageIndex(),
      size: PAGE_SIZE,
    })),
    offlinePumpsSortState: computed<'none' | 'asc' | 'desc'>(() =>
      sortBy() !== 'OFFLINE_PUMPS' ? 'none' : direction() === 'DESC' ? 'desc' : 'asc',
    ),
  })),
  withMethods((store, storeApi = inject(StoreApiService)) => ({
    loadStores: rxMethod<StoreQuery>(
      pipe(
        switchMap((query) => {
          patchState(store, { loading: true, error: null });
          return storeApi.list(query).pipe(
            tapResponse({
              next: (page) => patchState(store, { page, loading: false }),
              error: (error: unknown) =>
                patchState(store, { loading: false, error: toErrorMessage(error, 'Something went wrong loading stores.') }),
            }),
          );
        }),
      ),
    ),
    setBrand(brand: string): void {
      patchState(store, { brand, pageIndex: 0 });
    },
    setStatus(status: StoreStatus | ''): void {
      patchState(store, { status, pageIndex: 0 });
    },
    /**
     * Three-state cycle: unsorted -> descending -> ascending -> unsorted. "Unsorted" falls back to
     * the store id ordering rather than an undefined one, so the list is always predictable.
     */
    toggleOfflinePumpsSort(): void {
      if (store.sortBy() !== 'OFFLINE_PUMPS') {
        patchState(store, { sortBy: 'OFFLINE_PUMPS', direction: 'DESC', pageIndex: 0 });
      } else if (store.direction() === 'DESC') {
        patchState(store, { direction: 'ASC', pageIndex: 0 });
      } else {
        patchState(store, { sortBy: 'STORE_ID', direction: 'ASC', pageIndex: 0 });
      }
    },
    nextPage(): void {
      patchState(store, { pageIndex: store.pageIndex() + 1 });
    },
    previousPage(): void {
      patchState(store, { pageIndex: Math.max(0, store.pageIndex() - 1) });
    },
  })),
  withHooks({
    onInit(store) {
      store.loadStores(store.query);
    },
  }),
);
