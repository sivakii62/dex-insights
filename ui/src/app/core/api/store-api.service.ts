import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import type { Page } from '../models/page.model';
import type { Store, StoreQuery } from '../models/store.model';
import { apiPaths } from './api.config';

/** Thin HTTP client for the stores endpoints. No caching: the dataset is small and read-only. */
@Injectable({ providedIn: 'root' })
export class StoreApiService {
  private readonly http = inject(HttpClient);

  list(query: StoreQuery): Observable<Page<Store>> {
    let params = new HttpParams();
    if (query.brand) params = params.set('brand', query.brand);
    if (query.status) params = params.set('status', query.status);
    if (query.sortBy) params = params.set('sortBy', query.sortBy);
    if (query.direction) params = params.set('direction', query.direction);
    if (query.page !== undefined) params = params.set('page', query.page);
    if (query.size !== undefined) params = params.set('size', query.size);

    return this.http.get<Page<Store>>(apiPaths.stores(), { params });
  }

  getById(storeId: string): Observable<Store> {
    return this.http.get<Store>(apiPaths.store(storeId));
  }
}
