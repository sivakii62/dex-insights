package com.dex.insights.store;

import com.dex.insights.domain.Store;

import java.util.Comparator;

/** Sortable store attributes exposed to API clients. */
public enum StoreSortField {

    STORE_ID(Comparator.comparing(Store::storeId)),
    OFFLINE_PUMPS(Comparator.comparingDouble(Store::offlinePumpRatio)),
    ANOMALY_COUNT(Comparator.comparingInt(Store::anomalyCount)),
    LAST_UPDATED(Comparator.comparing(Store::lastUpdatedTime, Comparator.nullsLast(Comparator.naturalOrder())));

    private final Comparator<Store> comparator;

    StoreSortField(Comparator<Store> comparator) {
        this.comparator = comparator;
    }

    /** Ascending comparator, tie-broken on store id so paging is stable. */
    public Comparator<Store> comparator() {
        return comparator.thenComparing(Store::storeId);
    }
}
