import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ApiError } from './api-error';

/**
 * Translates backend ProblemDetail responses (and network failures) into ApiError so every caller
 * gets a consistent, user-presentable error regardless of what went wrong.
 */
export const errorInterceptor: HttpInterceptorFn = (request, next) =>
  next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 0) {
          return throwError(() => new ApiError(0, 'Could not reach the server. Check your connection.'));
        }
        return throwError(() => ApiError.fromProblemDetail(error.status, error.error));
      }
      return throwError(() => error);
    }),
  );
