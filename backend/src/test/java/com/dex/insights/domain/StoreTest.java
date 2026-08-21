package com.dex.insights.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StoreTest {

    private static final Instant NOW = Instant.parse("2026-03-02T08:00:00.000Z");

    @Test
    void derivesPumpAvailabilityAndGuardsAgainstZeroTotals() {
        assertThat(store(10, 7, List.of()).pumpAvailability()).isEqualTo(0.7);
        assertThat(store(0, 0, List.of()).pumpAvailability()).isZero();
    }

    @Test
    void reportsTheEmptiestTank() {
        Store store = store(8, 8, List.of(
                new Tank("Regular", 10_000, 6_400, 3_600, NOW),
                new Tank("Diesel", 8_000, 900, 7_100, NOW)));

        assertThat(store.lowestTank()).get().extracting(Tank::gradeName).isEqualTo("Diesel");
        assertThat(store.lowestTank().orElseThrow().fillRatio()).isEqualTo(0.1125);
    }

    @Test
    void treatsMissingTanksAsAnEmptyList() {
        Store store = new Store("10001", "7-Eleven", StoreStatus.ONLINE, 8, 8, 0, false, NOW,
                new StoreAddress("TX", "Austin"), 30.2672, -97.7431, 0, null);

        assertThat(store.tanks()).isEmpty();
        assertThat(store.lowestTank()).isEmpty();
    }

    private static Store store(int totalPumps, int activePumps, List<Tank> tanks) {
        return new Store("10001", "7-Eleven", StoreStatus.ONLINE, totalPumps, activePumps,
                totalPumps - activePumps, false, NOW, new StoreAddress("TX", "Austin"),
                30.2672, -97.7431, 0, tanks);
    }
}
