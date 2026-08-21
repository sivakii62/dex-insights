package com.dex.insights.support;

import com.dex.insights.domain.Dataset;
import com.dex.insights.domain.Incident;
import com.dex.insights.domain.IncidentSeverity;
import com.dex.insights.domain.IncidentStatus;
import com.dex.insights.domain.Store;
import com.dex.insights.domain.StoreAddress;
import com.dex.insights.domain.StoreStatus;
import com.dex.insights.domain.Tank;
import com.dex.insights.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** A small dataset shaped like the real feed: one healthy store, one fully down, one degraded with a low tank. */
public final class TestData {

    public static final Instant NOW = Instant.parse("2026-03-02T08:00:00.000Z");

    private TestData() {
    }

    public static Dataset dataset() {
        return new Dataset(
                List.of(
                        new Store("10001", "7-Eleven", StoreStatus.ONLINE, 8, 8, 0, false, NOW,
                                new StoreAddress("TX", "Austin"), 30.2672, -97.7431, 0,
                                List.of(new Tank("Regular", 10_000, 6_400, 3_600, NOW))),
                        new Store("10004", "Speedway", StoreStatus.OFFLINE, 12, 0, 12, true, NOW,
                                new StoreAddress("OH", "Columbus"), 39.9612, -82.9988, 5,
                                List.of(new Tank("Regular", 15_000, 11_500, 3_500, NOW))),
                        new Store("10009", "Speedway", StoreStatus.DEGRADED, 8, 6, 2, true, NOW,
                                new StoreAddress("NC", "Charlotte"), 35.2271, -80.8431, 4,
                                List.of(new Tank("Regular", 10_000, 1_400, 8_600, NOW)))),
                List.of(
                        new Transaction("TX-10001-0001", "10001", "Regular", new BigDecimal("42.18"),
                                new BigDecimal("12.3"), 3, NOW, NOW.plusSeconds(230)),
                        new Transaction("TX-10004-0001", "10004", "Diesel", new BigDecimal("88.40"),
                                new BigDecimal("24.1"), 1, NOW, NOW.plusSeconds(410))),
                List.of(
                        new Incident("INC-10004-0001", "10004", NOW, IncidentSeverity.HIGH, "NETWORK",
                                "Site controller unreachable; all dispensers offline.", IncidentStatus.OPEN),
                        new Incident("INC-10009-0001", "10009", NOW, IncidentSeverity.HIGH, "PUMP",
                                "Two dispensers reporting a hardware fault.", IncidentStatus.OPEN),
                        new Incident("INC-10001-0001", "10001", NOW, IncidentSeverity.LOW, "POS",
                                "Receipt printer jam cleared by site staff.", IncidentStatus.RESOLVED)));
    }
}
