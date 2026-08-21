import { ApiError } from './api-error';

describe('ApiError.fromProblemDetail', () => {
  it('prefers the detail message when present', () => {
    const error = ApiError.fromProblemDetail(404, { title: 'Resource not found', detail: "Store '99999' was not found" });

    expect(error.status).toBe(404);
    expect(error.message).toBe("Store '99999' was not found");
  });

  it('falls back to the title, then a generic message, when detail is missing', () => {
    expect(ApiError.fromProblemDetail(500, { title: 'Internal server error' }).message).toBe('Internal server error');
    expect(ApiError.fromProblemDetail(500, null).message).toBe('Request failed with status 500.');
  });

  it('carries the request id and field errors through for display', () => {
    const error = ApiError.fromProblemDetail(400, {
      detail: 'One or more fields failed validation.',
      requestId: 'abc-123',
      errors: { question: 'question must not be blank' },
    });

    expect(error.requestId).toBe('abc-123');
    expect(error.fieldErrors).toEqual({ question: 'question must not be blank' });
  });
});
