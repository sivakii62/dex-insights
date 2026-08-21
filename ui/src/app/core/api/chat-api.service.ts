import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import type { ChatRequest, ChatResponse } from '../models/chat.model';
import { apiPaths } from './api.config';

/** Thin HTTP client for the chat endpoint. */
@Injectable({ providedIn: 'root' })
export class ChatApiService {
  private readonly http = inject(HttpClient);

  ask(request: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(apiPaths.chat(), request);
  }
}
