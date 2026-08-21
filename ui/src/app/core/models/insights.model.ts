import type { StoreStatus } from './store.model';

/** Mirrors the backend's InsightsOverview record and its nested types. */

export interface FleetSummary {
  readonly totalStores: number;
  readonly storesByStatus: Partial<Record<StoreStatus, number>>;
  readonly totalPumps: number;
  readonly offlinePumps: number;
  readonly pumpAvailability: number;
  readonly hyperCareStores: number;
  readonly activeIncidents: number;
}

export interface OfflinePumpStore {
  readonly storeId: string;
  readonly brand: string;
  readonly location: string;
  readonly status: StoreStatus;
  readonly offlinePumps: number;
  readonly totalPumps: number;
  readonly openIncidents: number;
}

export interface TankRisk {
  readonly storeId: string;
  readonly brand: string;
  readonly location: string;
  readonly gradeName: string;
  readonly levelGallons: number;
  readonly capacityGallons: number;
  readonly fillRatio: number;
}

export type IncidentSeverity = 'HIGH' | 'MEDIUM' | 'LOW';
export type IncidentStatus = 'OPEN' | 'ACKNOWLEDGED' | 'RESOLVED';

export interface IncidentBreakdown {
  readonly total: number;
  readonly active: number;
  readonly bySeverity: Partial<Record<IncidentSeverity, number>>;
  readonly byStatus: Partial<Record<IncidentStatus, number>>;
  readonly byCategory: Record<string, number>;
}

export interface InsightsOverview {
  readonly generatedAt: string;
  readonly fleet: FleetSummary;
  readonly topStoresByOfflinePumps: readonly OfflinePumpStore[];
  readonly tankRunoutRisks: readonly TankRisk[];
  readonly incidents: IncidentBreakdown;
}
