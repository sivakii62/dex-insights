/** Mirrors the backend's RFC 9457 ProblemDetail error body. */
export interface ProblemDetail {
  readonly type?: string;
  readonly title?: string;
  readonly status?: number;
  readonly detail?: string;
  readonly requestId?: string;
  readonly errors?: Record<string, string>;
}
