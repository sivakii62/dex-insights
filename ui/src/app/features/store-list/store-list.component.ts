import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { ErrorStateComponent } from '../../shared/ui/error-state.component';
import { LoadingStateComponent } from '../../shared/ui/loading-state.component';
import { StatusBadgeComponent } from '../../shared/ui/status-badge.component';
import { STORE_STATUSES, StoreStatus } from '../../core/models/store.model';
import { StoreListStore } from './store-list.store';

/** Store list with brand/status filters, sorting and pagination. State lives in StoreListStore. */
@Component({
  selector: 'app-store-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, RouterLink, LoadingStateComponent, ErrorStateComponent, StatusBadgeComponent],
  providers: [StoreListStore],
  templateUrl: './store-list.component.html',
  styleUrl: './store-list.component.scss',
})
export class StoreListComponent {
  protected readonly store = inject(StoreListStore);

  protected readonly statuses = STORE_STATUSES;

  protected updateBrand(value: string): void {
    this.store.setBrand(value);
  }

  protected updateStatus(value: string): void {
    this.store.setStatus(value as StoreStatus | '');
  }
}
