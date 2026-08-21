package com.dex.insights.repository;

import com.dex.insights.domain.Dataset;
import com.dex.insights.domain.Incident;
import com.dex.insights.domain.IncidentSeverity;
import com.dex.insights.domain.IncidentStatus;
import com.dex.insights.domain.Store;
import com.dex.insights.domain.StoreAddress;
import com.dex.insights.domain.StoreStatus;
import com.dex.insights.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRepositoriesTest {

    private static final Instant NOW = Instant.parse("2026-03-02T08:00:00.000Z");

    private static final Dataset DATASET = new Dataset(
            List.of(store("10001"), store("10002")),
            List.of(transaction("TX-1", "10001"), transaction("TX-2", "10001")),
            List.of(incident("INC-1", "10002")));

    @Test
    void findsAStoreByIdAndReportsAbsenceAsEmpty() {
        StoreRepository repository = new InMemoryStoreRepository(DATASET);

        assertThat(repository.findById("10001")).get().extracting(Store::brand).isEqualTo("7-Eleven");
        assertThat(repository.findById("99999")).isEmpty();
        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void groupsChildRecordsByStoreAndReturnsEmptyForUnknownStores() {
        TransactionRepository transactions = new InMemoryTransactionRepository(DATASET);
        IncidentRepository incidents = new InMemoryIncidentRepository(DATASET);

        assertThat(transactions.findByStoreId("10001")).hasSize(2);
        assertThat(transactions.findByStoreId("10002")).isEmpty();
        assertThat(incidents.findByStoreId("10002")).singleElement()
                .extracting(Incident::severity).isEqualTo(IncidentSeverity.HIGH);
        assertThat(incidents.findByStoreId("99999")).isEmpty();
    }

    private static Store store(String id) {
        return new Store(id, "7-Eleven", StoreStatus.ONLINE, 8, 8, 0, false, NOW,
                new StoreAddress("TX", "Austin"), 30.2672, -97.7431, 0, List.of());
    }

    private static Transaction transaction(String id, String storeId) {
        return new Transaction(id, storeId, "Regular", new BigDecimal("10.00"), new BigDecimal("3.0"), 1,
                NOW, NOW.plusSeconds(120));
    }

    private static Incident incident(String id, String storeId) {
        return new Incident(id, storeId, NOW, IncidentSeverity.HIGH, "PUMP", "Dispenser offline",
                IncidentStatus.OPEN);
    }
}
