package com.dex.insights.insights;

import com.dex.insights.domain.IncidentSeverity;
import com.dex.insights.domain.IncidentStatus;
import com.dex.insights.domain.StoreStatus;
import com.dex.insights.insights.InsightsOverview.OfflinePumpStore;
import com.dex.insights.repository.InMemoryIncidentRepository;
import com.dex.insights.repository.InMemoryStoreRepository;
import com.dex.insights.support.TestData;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class InsightsServiceTest {

    private final InsightsService service = new InsightsService(
            new InMemoryStoreRepository(TestData.dataset()),
            new InMemoryIncidentRepository(TestData.dataset()),
            new InsightsProperties(0.25d, 5),
            Clock.fixed(TestData.NOW, ZoneOffset.UTC));

    @Test
    void rollsUpFleetAvailability() {
        InsightsOverview.FleetSummary fleet = service.overview().fleet();

        assertThat(fleet.totalStores()).isEqualTo(3);
        assertThat(fleet.storesByStatus())
                .containsEntry(StoreStatus.ONLINE, 1L)
                .containsEntry(StoreStatus.DEGRADED, 1L)
                .containsEntry(StoreStatus.OFFLINE, 1L);
        assertThat(fleet.totalPumps()).isEqualTo(28);
        assertThat(fleet.offlinePumps()).isEqualTo(14);
        assertThat(fleet.pumpAvailability()).isCloseTo(0.5d, within(0.0001d));
        assertThat(fleet.hyperCareStores()).isEqualTo(2);
        assertThat(fleet.activeIncidents()).isEqualTo(2);
    }

    @Test
    void ranksStoresByOfflinePumpsAndCountsTheirOpenIncidents() {
        assertThat(service.topStoresByOfflinePumps())
                .extracting(OfflinePumpStore::storeId).containsExactly("10004", "10009");

        assertThat(service.topStoresByOfflinePumps().getFirst())
                .satisfies(store -> {
                    assertThat(store.offlinePumps()).isEqualTo(12);
                    assertThat(store.openIncidents()).isEqualTo(1);
                    assertThat(store.location()).isEqualTo("Columbus, OH");
                });
    }

    @Test
    void flagsOnlyTanksAtOrBelowTheConfiguredThreshold() {
        assertThat(service.tankRunoutRisks())
                .singleElement()
                .satisfies(risk -> {
                    assertThat(risk.storeId()).isEqualTo("10009");
                    assertThat(risk.fillRatio()).isCloseTo(0.14d, within(0.0001d));
                });
    }

    @Test
    void countsIncidentsBySeverityStatusAndCategory() {
        InsightsOverview.IncidentBreakdown breakdown = service.incidentBreakdown();

        assertThat(breakdown.total()).isEqualTo(3);
        assertThat(breakdown.active()).isEqualTo(2);
        assertThat(breakdown.bySeverity())
                .containsEntry(IncidentSeverity.HIGH, 2L)
                .containsEntry(IncidentSeverity.MEDIUM, 0L)
                .containsEntry(IncidentSeverity.LOW, 1L);
        assertThat(breakdown.byStatus()).containsEntry(IncidentStatus.OPEN, 2L);
        assertThat(breakdown.byCategory()).containsEntry("NETWORK", 1L).containsEntry("PUMP", 1L);
    }

    @Test
    void exposesTheThresholdItFilteredAgainst() {
        assertThat(service.lowTankThreshold()).isEqualTo(0.25d);
        assertThat(service.tankRunoutRisks()).allSatisfy(risk ->
                assertThat(risk.fillRatio()).isLessThanOrEqualTo(service.lowTankThreshold()));
    }
}
