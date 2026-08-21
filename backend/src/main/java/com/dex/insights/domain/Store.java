package com.dex.insights.domain;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Store operational status snapshot.
 *
 * <p>The source feed uses inconsistent field names (STOREID, BRAND); {@link JsonAlias} accepts
 * those on the way in while the API still serializes the clean camelCase names on the way out.
 */
public record Store(
        @JsonAlias("STOREID") String storeId,
        @JsonAlias("BRAND") String brand,
        StoreStatus status,
        int totalPumps,
        int activePumps,
        int offlinePumps,
        boolean hyperCare,
        Instant lastUpdatedTime,
        StoreAddress storeAddress,
        Double latitude,
        Double longitude,
        int anomalyCount,
        List<Tank> tanks) {

    public Store {
        Objects.requireNonNull(storeId, "storeId is required");
        tanks = tanks == null ? List.of() : List.copyOf(tanks);
    }

    /** Share of pumps currently dispensing, in the range 0..1. */
    public double pumpAvailability() {
        return totalPumps <= 0 ? 0d : (double) activePumps / totalPumps;
    }

    /** The lowest tank fill ratio at this store, or empty when no gauge readings exist. */
    public Optional<Tank> lowestTank() {
        return tanks.stream().min(Comparator.comparingDouble(Tank::fillRatio));
    }
}
