import { inject } from '@angular/core';
import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { tapResponse } from '@ngrx/operators';
import { pipe, switchMap } from 'rxjs';

import { ChatApiService } from '../../core/api/chat-api.service';
import { toErrorMessage } from '../../core/api/to-error-message';
import type { ChatRequest, ChatResponse } from '../../core/models/chat.model';

interface ChatState {
  response: ChatResponse | null;
  loading: boolean;
  error: string | null;
}

const initialState: ChatState = { response: null, loading: false, error: null };

/** Owns the chat request/response state; triggered explicitly by the form, not on init. */
export const ChatStore = signalStore(
  withState(initialState),
  withMethods((store, chatApi = inject(ChatApiService)) => ({
    ask: rxMethod<ChatRequest>(
      pipe(
        switchMap((request) => {
          patchState(store, { loading: true, error: null });
          return chatApi.ask(request).pipe(
            tapResponse({
              next: (response) => patchState(store, { response, loading: false }),
              error: (error: unknown) =>
                patchState(store, { loading: false, error: toErrorMessage(error, 'Something went wrong answering that.') }),
            }),
          );
        }),
      ),
    ),
  })),
);
