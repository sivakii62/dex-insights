package com.dex.insights.repository;

import com.dex.insights.domain.Dataset;
import com.dex.insights.domain.Incident;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Serves incidents from the startup snapshot, grouped by store for O(1) lookup. */
@Repository
public class InMemoryIncidentRepository implements IncidentRepository {

    private final List<Incident> incidents;
    private final Map<String, List<Incident>> byStoreId;

    public InMemoryIncidentRepository(Dataset dataset) {
        this.incidents = dataset.incidents();
        this.byStoreId = incidents.stream()
                .collect(Collectors.groupingBy(Incident::storeId,
                        Collectors.collectingAndThen(Collectors.toList(), List::copyOf)));
    }

    @Override
    public List<Incident> findAll() {
        return incidents;
    }

    @Override
    public List<Incident> findByStoreId(String storeId) {
        return byStoreId.getOrDefault(storeId, List.of());
    }
}
