import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import type { InsightsOverview } from '../models/insights.model';

/** Thin HTTP client for the /v1/insights endpoints. */
@Injectable({ providedIn: 'root' })
export class InsightsApiService {
  private readonly http = inject(HttpClient);

  overview(): Observable<InsightsOverview> {
    return this.http.get<InsightsOverview>('/v1/insights/overview');
  }
}
