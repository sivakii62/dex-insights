import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { KeyValuePipe, PercentPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ErrorStateComponent } from '../../shared/ui/error-state.component';
import { LoadingStateComponent } from '../../shared/ui/loading-state.component';
import { StatusBadgeComponent } from '../../shared/ui/status-badge.component';
import { InsightsStore } from './insights.store';

/** Fleet-level operational overview. State lives in InsightsStore. */
@Component({
  selector: 'app-insights',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, PercentPipe, KeyValuePipe, LoadingStateComponent, ErrorStateComponent, StatusBadgeComponent],
  providers: [InsightsStore],
  templateUrl: './insights.component.html',
  styleUrl: './insights.component.scss',
})
export class InsightsComponent {
  protected readonly store = inject(InsightsStore);

  /** Severity keys map onto the same tone vocabulary as status badges elsewhere. */
  protected severityTone(severity: string): 'positive' | 'warning' | 'critical' {
    if (severity === 'HIGH') return 'critical';
    if (severity === 'MEDIUM') return 'warning';
    return 'positive';
  }
}
