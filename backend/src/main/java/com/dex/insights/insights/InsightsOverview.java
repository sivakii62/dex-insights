package com.dex.insights.insights;

import com.dex.insights.domain.IncidentSeverity;
import com.dex.insights.domain.IncidentStatus;
import com.dex.insights.domain.StoreStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** The operational overview returned by {@code GET /v1/insights/overview}. */
public record InsightsOverview(
        Instant generatedAt,
        FleetSummary fleet,
        List<OfflinePumpStore> topStoresByOfflinePumps,
        List<TankRisk> tankRunoutRisks,
        IncidentBreakdown incidents) {

    /** Fleet-wide roll-up of store and pump availability. */
    public record FleetSummary(
            int totalStores,
            Map<StoreStatus, Long> storesByStatus,
            int totalPumps,
            int offlinePumps,
            double pumpAvailability,
            int hyperCareStores,
            long activeIncidents) {
    }

    /** A store ranked by how many of its pumps are down. */
    public record OfflinePumpStore(
            String storeId,
            String brand,
            String location,
            StoreStatus status,
            int offlinePumps,
            int totalPumps,
            long openIncidents) {
    }

    /** A tank at or below the configured low-level threshold. */
    public record TankRisk(
            String storeId,
            String brand,
            String location,
            String gradeName,
            int levelGallons,
            int capacityGallons,
            double fillRatio) {
    }

    /** Incident counts sliced the three ways operators ask for. */
    public record IncidentBreakdown(
            long total,
            long active,
            Map<IncidentSeverity, Long> bySeverity,
            Map<IncidentStatus, Long> byStatus,
            Map<String, Long> byCategory) {
    }
}
