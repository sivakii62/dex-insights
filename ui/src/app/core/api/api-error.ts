import type { ProblemDetail } from '../models/problem-detail.model';

/**
 * A normalized, display-ready error. Components depend on this rather than the raw HttpErrorResponse
 * so the "what do I show the user" decision lives in one place.
 */
export class ApiError extends Error {
  readonly status: number;
  readonly requestId?: string;
  readonly fieldErrors?: Record<string, string>;

  constructor(status: number, message: string, requestId?: string, fieldErrors?: Record<string, string>) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.requestId = requestId;
    this.fieldErrors = fieldErrors;
  }

  static fromProblemDetail(status: number, problem: ProblemDetail | null): ApiError {
    const message = problem?.detail ?? problem?.title ?? `Request failed with status ${status}.`;
    return new ApiError(status, message, problem?.requestId, problem?.errors);
  }
}
