import { inject } from '@angular/core';
import { patchState, signalStore, withHooks, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { tapResponse } from '@ngrx/operators';
import { pipe, switchMap } from 'rxjs';

import { InsightsApiService } from '../../core/api/insights-api.service';
import { toErrorMessage } from '../../core/api/to-error-message';
import type { InsightsOverview } from '../../core/models/insights.model';

interface InsightsState {
  overview: InsightsOverview | null;
  loading: boolean;
  error: string | null;
}

const initialState: InsightsState = { overview: null, loading: true, error: null };

/** Owns the fleet overview state and loads it once on init; provided per-route. */
export const InsightsStore = signalStore(
  withState(initialState),
  withMethods((store, insightsApi = inject(InsightsApiService)) => ({
    loadOverview: rxMethod<void>(
      pipe(
        switchMap(() => {
          patchState(store, { loading: true, error: null });
          return insightsApi.overview().pipe(
            tapResponse({
              next: (overview) => patchState(store, { overview, loading: false }),
              error: (error: unknown) =>
                patchState(store, { loading: false, error: toErrorMessage(error, 'Something went wrong loading insights.') }),
            }),
          );
        }),
      ),
    ),
  })),
  withHooks({
    onInit(store) {
      store.loadOverview();
    },
  }),
);
