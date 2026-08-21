import { inject } from '@angular/core';
import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { tapResponse } from '@ngrx/operators';
import { pipe, switchMap } from 'rxjs';

import { StoreApiService } from '../../core/api/store-api.service';
import { toErrorMessage } from '../../core/api/to-error-message';
import type { Store } from '../../core/models/store.model';

interface StoreDetailState {
  store: Store | null;
  loading: boolean;
  error: string | null;
}

const initialState: StoreDetailState = { store: null, loading: true, error: null };

/**
 * Owns a single store's detail state, keyed by the storeId the routed component feeds it.
 * Provided per-route (see StoreDetailComponent) so navigating between stores starts clean.
 */
export const StoreDetailStore = signalStore(
  withState(initialState),
  withMethods((store, storeApi = inject(StoreApiService)) => ({
    loadStore: rxMethod<string>(
      pipe(
        switchMap((storeId) => {
          patchState(store, { loading: true, error: null });
          return storeApi.getById(storeId).pipe(
            tapResponse({
              next: (result) => patchState(store, { store: result, loading: false }),
              error: (error: unknown) =>
                patchState(store, { loading: false, error: toErrorMessage(error, 'Something went wrong loading this store.') }),
            }),
          );
        }),
      ),
    ),
  })),
);
