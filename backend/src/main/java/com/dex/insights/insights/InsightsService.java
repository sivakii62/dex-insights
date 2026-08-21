package com.dex.insights.insights;

import com.dex.insights.domain.Incident;
import com.dex.insights.domain.IncidentSeverity;
import com.dex.insights.domain.IncidentStatus;
import com.dex.insights.domain.Store;
import com.dex.insights.domain.StoreStatus;
import com.dex.insights.domain.Tank;
import com.dex.insights.insights.InsightsOverview.FleetSummary;
import com.dex.insights.insights.InsightsOverview.IncidentBreakdown;
import com.dex.insights.insights.InsightsOverview.OfflinePumpStore;
import com.dex.insights.insights.InsightsOverview.TankRisk;
import com.dex.insights.repository.IncidentRepository;
import com.dex.insights.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Derives the operational insights surfaced on the overview endpoint and reused by the chat answers. */
@Service
public class InsightsService {

    private final StoreRepository storeRepository;
    private final IncidentRepository incidentRepository;
    private final InsightsProperties properties;
    private final Clock clock;

    public InsightsService(StoreRepository storeRepository, IncidentRepository incidentRepository,
                           InsightsProperties properties, Clock clock) {
        this.storeRepository = storeRepository;
        this.incidentRepository = incidentRepository;
        this.properties = properties;
        this.clock = clock;
    }

    public InsightsOverview overview() {
        return new InsightsOverview(
                clock.instant(),
                fleetSummary(),
                topStoresByOfflinePumps(),
                tankRunoutRisks(),
                incidentBreakdown());
    }

    /** Stores with pumps down, worst first. */
    public List<OfflinePumpStore> topStoresByOfflinePumps() {
        Map<String, Long> openIncidentsByStore = incidentRepository.findAll().stream()
                .filter(Incident::isActive)
                .collect(Collectors.groupingBy(Incident::storeId, Collectors.counting()));

        return storeRepository.findAll().stream()
                .filter(store -> store.offlinePumps() > 0)
                .sorted(Comparator.comparingInt(Store::offlinePumps).reversed()
                        .thenComparing(Store::storeId))
                .limit(properties.topN())
                .map(store -> new OfflinePumpStore(
                        store.storeId(),
                        store.brand(),
                        location(store),
                        store.status(),
                        store.offlinePumps(),
                        store.totalPumps(),
                        openIncidentsByStore.getOrDefault(store.storeId(), 0L)))
                .toList();
    }

    /** Tanks at or below the configured fill threshold, emptiest first. */
    public List<TankRisk> tankRunoutRisks() {
        return storeRepository.findAll().stream()
                .flatMap(store -> store.tanks().stream()
                        .filter(tank -> tank.fillRatio() <= properties.lowTankThreshold())
                        .map(tank -> toTankRisk(store, tank)))
                .sorted(Comparator.comparingDouble(TankRisk::fillRatio))
                .limit(properties.topN())
                .toList();
    }

    public IncidentBreakdown incidentBreakdown() {
        List<Incident> incidents = incidentRepository.findAll();

        Map<IncidentSeverity, Long> bySeverity = new EnumMap<>(IncidentSeverity.class);
        for (IncidentSeverity severity : IncidentSeverity.values()) {
            bySeverity.put(severity, 0L);
        }
        Map<IncidentStatus, Long> byStatus = new EnumMap<>(IncidentStatus.class);
        for (IncidentStatus status : IncidentStatus.values()) {
            byStatus.put(status, 0L);
        }
        incidents.forEach(incident -> {
            if (incident.severity() != null) {
                bySeverity.merge(incident.severity(), 1L, Long::sum);
            }
            if (incident.status() != null) {
                byStatus.merge(incident.status(), 1L, Long::sum);
            }
        });

        Map<String, Long> byCategory = incidents.stream()
                .filter(incident -> incident.category() != null)
                .collect(Collectors.groupingBy(Incident::category, LinkedHashMap::new, Collectors.counting()));

        return new IncidentBreakdown(
                incidents.size(),
                incidents.stream().filter(Incident::isActive).count(),
                bySeverity,
                byStatus,
                byCategory);
    }

    private FleetSummary fleetSummary() {
        List<Store> stores = storeRepository.findAll();

        Map<StoreStatus, Long> byStatus = new EnumMap<>(StoreStatus.class);
        for (StoreStatus status : StoreStatus.values()) {
            byStatus.put(status, 0L);
        }
        stores.stream().map(Store::status).filter(Objects::nonNull)
                .forEach(status -> byStatus.merge(status, 1L, Long::sum));

        int totalPumps = stores.stream().mapToInt(Store::totalPumps).sum();
        int offlinePumps = stores.stream().mapToInt(Store::offlinePumps).sum();

        return new FleetSummary(
                stores.size(),
                byStatus,
                totalPumps,
                offlinePumps,
                totalPumps == 0 ? 0d : (double) (totalPumps - offlinePumps) / totalPumps,
                (int) stores.stream().filter(Store::hyperCare).count(),
                incidentRepository.findAll().stream().filter(Incident::isActive).count());
    }

    private TankRisk toTankRisk(Store store, Tank tank) {
        return new TankRisk(store.storeId(), store.brand(), location(store), tank.gradeName(),
                tank.levelGallons(), tank.capacityGallons(), tank.fillRatio());
    }

    private static String location(Store store) {
        return store.storeAddress() == null ? "Unknown location" : store.storeAddress().label();
    }

    /** Exposed so callers can format the same threshold they were filtered against. */
    public double lowTankThreshold() {
        return properties.lowTankThreshold();
    }
}
