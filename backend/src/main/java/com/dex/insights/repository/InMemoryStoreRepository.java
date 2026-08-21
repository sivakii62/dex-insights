package com.dex.insights.repository;

import com.dex.insights.domain.Dataset;
import com.dex.insights.domain.Store;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Serves stores from the startup snapshot; the id index is built once and never mutated. */
@Repository
public class InMemoryStoreRepository implements StoreRepository {

    private final List<Store> stores;
    private final Map<String, Store> byId;

    public InMemoryStoreRepository(Dataset dataset) {
        this.stores = dataset.stores();
        this.byId = stores.stream().collect(Collectors.toUnmodifiableMap(Store::storeId, Function.identity()));
    }

    @Override
    public List<Store> findAll() {
        return stores;
    }

    @Override
    public Optional<Store> findById(String storeId) {
        return Optional.ofNullable(byId.get(storeId));
    }
}
