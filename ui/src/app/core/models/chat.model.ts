/** Mirrors the backend's ChatRequest/ChatResponse and Citation records. */

export interface ChatRequest {
  readonly question: string;
  readonly storeId?: string;
}

export type CitationType = 'STORE' | 'INCIDENT' | 'TRANSACTION';

export interface Citation {
  readonly type: CitationType;
  readonly recordId: string;
  readonly storeId: string | null;
  readonly timestamp: string | null;
  readonly snippet: string;
  readonly relevance: number;
}

export interface ChatResponse {
  readonly answer: string;
  readonly citations: readonly Citation[];
  readonly retrievedContextSummary: string;
}
