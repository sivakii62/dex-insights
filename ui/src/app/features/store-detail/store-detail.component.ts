import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { rxResource } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';

import { StoreApiService } from '../../core/api/store-api.service';
import { ApiError } from '../../core/api/api-error';
import { ErrorStateComponent } from '../../shared/ui/error-state.component';
import { LoadingStateComponent } from '../../shared/ui/loading-state.component';
import { StatusBadgeComponent } from '../../shared/ui/status-badge.component';

/**
 * Store detail, backed by GET /v1/stores/{storeId}.
 * `storeId` is bound directly from the route param (withComponentInputBinding).
 */
@Component({
  selector: 'app-store-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, DecimalPipe, LoadingStateComponent, ErrorStateComponent, StatusBadgeComponent],
  templateUrl: './store-detail.component.html',
  styleUrl: './store-detail.component.scss',
})
export class StoreDetailComponent {
  private readonly storeApi = inject(StoreApiService);

  readonly storeId = input.required<string>();

  protected readonly storeResource = rxResource({
    params: this.storeId,
    stream: ({ params }) => this.storeApi.getById(params),
  });

  protected readonly errorMessage = computed(() => {
    const error = this.storeResource.error();
    return error instanceof ApiError ? error.message : error ? 'Something went wrong loading this store.' : null;
  });
}
