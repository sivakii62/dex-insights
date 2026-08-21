import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ErrorStateComponent } from '../../shared/ui/error-state.component';
import { LoadingStateComponent } from '../../shared/ui/loading-state.component';
import { StatusBadgeComponent } from '../../shared/ui/status-badge.component';
import { StoreDetailStore } from './store-detail.store';

/**
 * Store detail. `storeId` is bound directly from the route param (withComponentInputBinding) and
 * fed into StoreDetailStore, which owns the fetch state.
 */
@Component({
  selector: 'app-store-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, DecimalPipe, LoadingStateComponent, ErrorStateComponent, StatusBadgeComponent],
  providers: [StoreDetailStore],
  templateUrl: './store-detail.component.html',
  styleUrl: './store-detail.component.scss',
})
export class StoreDetailComponent {
  protected readonly store = inject(StoreDetailStore);

  readonly storeId = input.required<string>();

  constructor() {
    this.store.loadStore(this.storeId);
  }
}
