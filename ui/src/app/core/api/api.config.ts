/**
 * Single source of truth for the API version and every endpoint path. Services build URLs from
 * here instead of writing `/v1/...` literals, so bumping the API version or moving a resource is a
 * one-line change instead of a grep across the codebase.
 *
 * The prefix is relative ('/v1', not an absolute origin) by design: the Angular dev server proxies
 * it to the backend (see proxy.conf.json) and the production nginx image reverse-proxies the same
 * prefix (see ui/nginx.conf), so the app never needs to know the backend's actual host.
 */
const API_VERSION = 'v1';
const API_ROOT = `/${API_VERSION}`;

export const apiPaths = {
  stores: () => `${API_ROOT}/stores`,
  store: (storeId: string) => `${API_ROOT}/stores/${encodeURIComponent(storeId)}`,
  insightsOverview: () => `${API_ROOT}/insights/overview`,
  chat: () => `${API_ROOT}/chat`,
} as const;
