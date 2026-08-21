import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import type { ChatRequest, ChatResponse } from '../models/chat.model';

/** Thin HTTP client for the /v1/chat endpoint. */
@Injectable({ providedIn: 'root' })
export class ChatApiService {
  private readonly http = inject(HttpClient);

  ask(request: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>('/v1/chat', request);
  }
}
