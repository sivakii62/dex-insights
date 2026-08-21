package com.dex.insights.domain;

import java.util.List;

/**
 * The immutable snapshot of source data held in memory for the lifetime of the application.
 * The dataset is small and read-only, so loading it once at startup avoids a datastore entirely.
 */
public record Dataset(List<Store> stores, List<Transaction> transactions, List<Incident> incidents) {

    public Dataset {
        stores = List.copyOf(stores);
        transactions = List.copyOf(transactions);
        incidents = List.copyOf(incidents);
    }
}
