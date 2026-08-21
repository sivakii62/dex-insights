/** Mirrors the backend's Store record (com.dex.insights.domain.Store). */

export type StoreStatus = 'ONLINE' | 'DEGRADED' | 'OFFLINE';

export interface StoreAddress {
  readonly state: string | null;
  readonly city: string | null;
}

export interface Tank {
  readonly gradeName: string;
  readonly capacityGallons: number;
  readonly levelGallons: number;
  readonly ullageGallons: number;
  readonly lastUpdatedTime: string;
}

export interface Store {
  readonly storeId: string;
  readonly brand: string;
  readonly status: StoreStatus;
  readonly totalPumps: number;
  readonly activePumps: number;
  readonly offlinePumps: number;
  readonly hyperCare: boolean;
  readonly lastUpdatedTime: string;
  readonly storeAddress: StoreAddress | null;
  readonly latitude: number | null;
  readonly longitude: number | null;
  readonly anomalyCount: number;
  readonly tanks: readonly Tank[];
}

export const STORE_STATUSES: readonly StoreStatus[] = ['ONLINE', 'DEGRADED', 'OFFLINE'];

export type StoreSortField = 'STORE_ID' | 'OFFLINE_PUMPS' | 'ANOMALY_COUNT' | 'LAST_UPDATED';
export type SortDirection = 'ASC' | 'DESC';

export interface StoreQuery {
  readonly brand?: string;
  readonly status?: StoreStatus;
  readonly sortBy?: StoreSortField;
  readonly direction?: SortDirection;
  readonly page?: number;
  readonly size?: number;
}
