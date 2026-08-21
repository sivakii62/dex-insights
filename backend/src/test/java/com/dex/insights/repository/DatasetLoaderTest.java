package com.dex.insights.repository;

import com.dex.insights.config.DatasetProperties;
import com.dex.insights.domain.Dataset;
import com.dex.insights.domain.IncidentSeverity;
import com.dex.insights.domain.Store;
import com.dex.insights.domain.StoreStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatasetLoaderTest {

    private DatasetLoader loader;

    @BeforeEach
    void setUp() {
        loader = new DatasetLoader(new ObjectMapper().registerModule(new JavaTimeModule()));
    }

    private DatasetProperties bundled() {
        return new DatasetProperties(
                new ClassPathResource("data/stores.json"),
                new ClassPathResource("data/transactions.json"),
                new ClassPathResource("data/incidents.json"));
    }

    @Test
    void loadsEveryRecordFromTheBundledDataset() {
        Dataset dataset = loader.load(bundled());

        assertThat(dataset.stores()).hasSize(10);
        assertThat(dataset.transactions()).hasSize(15);
        assertThat(dataset.incidents()).hasSize(8);
    }

    @Test
    void mapsUppercaseSourceFieldsOntoTheDomainModel() {
        Store store = loader.load(bundled()).stores().stream()
                .filter(s -> s.storeId().equals("10001"))
                .findFirst()
                .orElseThrow();

        assertThat(store.brand()).isEqualTo("7-Eleven");
        assertThat(store.status()).isEqualTo(StoreStatus.ONLINE);
        assertThat(store.totalPumps()).isEqualTo(8);
        assertThat(store.storeAddress().city()).isEqualTo("Austin");
        assertThat(store.latitude()).isEqualTo(30.2672);
        assertThat(store.lastUpdatedTime()).isEqualTo(Instant.parse("2026-03-02T08:15:22.120Z"));
        assertThat(store.tanks()).hasSize(2);
    }

    @Test
    void parsesEnumsAndNumericStringsInRelatedFeeds() {
        Dataset dataset = loader.load(bundled());

        assertThat(dataset.incidents())
                .filteredOn(incident -> incident.incidentId().equals("INC-10002-0001"))
                .singleElement()
                .satisfies(incident -> {
                    assertThat(incident.severity()).isEqualTo(IncidentSeverity.HIGH);
                    assertThat(incident.isActive()).isTrue();
                });

        assertThat(dataset.transactions().getFirst().transactionAmnt()).isEqualByComparingTo("42.18");
    }

    @Test
    void failsFastWhenADataFileIsMissing() {
        DatasetProperties missing = new DatasetProperties(
                new ClassPathResource("data/does-not-exist.json"),
                new ClassPathResource("data/transactions.json"),
                new ClassPathResource("data/incidents.json"));

        assertThatThrownBy(() -> loader.load(missing))
                .isInstanceOf(DatasetLoadException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void failsFastOnDuplicateIdentifiers() {
        Resource duplicated = new ByteArrayResource("""
                [
                  {"STOREID":"1","BRAND":"A","status":"ONLINE"},
                  {"STOREID":"1","BRAND":"B","status":"ONLINE"}
                ]
                """.getBytes());

        DatasetProperties properties = new DatasetProperties(duplicated,
                new ClassPathResource("data/transactions.json"),
                new ClassPathResource("data/incidents.json"));

        assertThatThrownBy(() -> loader.load(properties))
                .isInstanceOf(DatasetLoadException.class)
                .hasMessageContaining("Duplicate store id");
    }
}
