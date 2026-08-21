import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { StoreApiService } from '../../core/api/store-api.service';
import { ApiError } from '../../core/api/api-error';
import { ErrorStateComponent } from '../../shared/ui/error-state.component';
import { LoadingStateComponent } from '../../shared/ui/loading-state.component';
import { StatusBadgeComponent } from '../../shared/ui/status-badge.component';
import { STORE_STATUSES, StoreSortField, StoreStatus, SortDirection } from '../../core/models/store.model';

const PAGE_SIZE = 10;

/** Store list with brand/status filters, sorting and pagination, backed by GET /v1/stores. */
@Component({
  selector: 'app-store-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, RouterLink, LoadingStateComponent, ErrorStateComponent, StatusBadgeComponent],
  templateUrl: './store-list.component.html',
  styleUrl: './store-list.component.scss',
})
export class StoreListComponent {
  private readonly storeApi = inject(StoreApiService);
  private readonly router = inject(Router);

  protected readonly statuses = STORE_STATUSES;

  protected readonly brand = signal('');
  protected readonly status = signal<StoreStatus | ''>('');
  protected readonly sortBy = signal<StoreSortField>('STORE_ID');
  protected readonly direction = signal<SortDirection>('ASC');
  protected readonly page = signal(0);

  private readonly query = computed(() => ({
    brand: this.brand().trim() || undefined,
    status: this.status() || undefined,
    sortBy: this.sortBy(),
    direction: this.direction(),
    page: this.page(),
    size: PAGE_SIZE,
  }));

  protected readonly storesResource = rxResource({
    params: this.query,
    stream: ({ params }) => this.storeApi.list(params),
  });

  protected readonly errorMessage = computed(() => {
    const error = this.storesResource.error();
    return error instanceof ApiError ? error.message : error ? 'Something went wrong loading stores.' : null;
  });

  protected updateBrand(value: string): void {
    this.brand.set(value);
    this.page.set(0);
  }

  protected updateStatus(value: string): void {
    this.status.set(value as StoreStatus | '');
    this.page.set(0);
  }

  /**
   * Three-state cycle: unsorted -> descending -> ascending -> unsorted. "Unsorted" falls back to
   * the store id ordering rather than an undefined one, so the list is always predictable.
   */
  protected toggleOfflinePumpsSort(): void {
    if (this.sortBy() !== 'OFFLINE_PUMPS') {
      this.sortBy.set('OFFLINE_PUMPS');
      this.direction.set('DESC');
    } else if (this.direction() === 'DESC') {
      this.direction.set('ASC');
    } else {
      this.sortBy.set('STORE_ID');
      this.direction.set('ASC');
    }
    this.page.set(0);
  }

  protected readonly offlinePumpsSortState = computed<'none' | 'asc' | 'desc'>(() =>
    this.sortBy() !== 'OFFLINE_PUMPS' ? 'none' : this.direction() === 'DESC' ? 'desc' : 'asc',
  );

  protected openStore(storeId: string): void {
    void this.router.navigate(['/stores', storeId]);
  }

  protected nextPage(): void {
    this.page.update((current) => current + 1);
  }

  protected previousPage(): void {
    this.page.update((current) => Math.max(0, current - 1));
  }
}
