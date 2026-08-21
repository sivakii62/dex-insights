import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { KeyValuePipe, PercentPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { InsightsApiService } from '../../core/api/insights-api.service';
import { ApiError } from '../../core/api/api-error';
import { ErrorStateComponent } from '../../shared/ui/error-state.component';
import { LoadingStateComponent } from '../../shared/ui/loading-state.component';
import { StatusBadgeComponent } from '../../shared/ui/status-badge.component';

/** Fleet-level operational overview, backed by GET /v1/insights/overview. */
@Component({
  selector: 'app-insights',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, PercentPipe, KeyValuePipe, LoadingStateComponent, ErrorStateComponent, StatusBadgeComponent],
  templateUrl: './insights.component.html',
  styleUrl: './insights.component.scss',
})
export class InsightsComponent {
  private readonly insightsApi = inject(InsightsApiService);

  protected readonly overviewResource = rxResource({
    stream: () => this.insightsApi.overview(),
  });

  protected readonly errorMessage = computed(() => {
    const error = this.overviewResource.error();
    return error instanceof ApiError ? error.message : error ? 'Something went wrong loading insights.' : null;
  });

  /** Severity keys map onto the same tone vocabulary as status badges elsewhere. */
  protected severityTone(severity: string): 'positive' | 'warning' | 'critical' {
    if (severity === 'HIGH') return 'critical';
    if (severity === 'MEDIUM') return 'warning';
    return 'positive';
  }
}
