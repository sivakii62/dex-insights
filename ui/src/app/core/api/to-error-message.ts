import { ApiError } from './api-error';

/** Every store's rxMethod error handler needs the same translation; this is the one place it lives. */
export function toErrorMessage(error: unknown, fallback: string): string {
  return error instanceof ApiError ? error.message : fallback;
}
