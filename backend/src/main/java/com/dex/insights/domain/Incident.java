package com.dex.insights.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * An operational incident raised against a store.
 * Category is intentionally a free-form string: the source feed treats it as an open set.
 */
public record Incident(
        String incidentId,
        String storeId,
        Instant timestamp,
        IncidentSeverity severity,
        String category,
        String description,
        IncidentStatus status) {

    public Incident {
        Objects.requireNonNull(incidentId, "incidentId is required");
        Objects.requireNonNull(storeId, "storeId is required");
    }

    /** True while the incident has not been resolved. */
    public boolean isActive() {
        return status != null && status.isActive();
    }
}
