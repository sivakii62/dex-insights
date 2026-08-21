package com.dex.insights.repository;

import com.dex.insights.config.DatasetProperties;
import com.dex.insights.domain.Dataset;
import com.dex.insights.domain.Incident;
import com.dex.insights.domain.Store;
import com.dex.insights.domain.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reads the JSON source files into an immutable {@link Dataset}.
 *
 * <p>Deliberately a plain class rather than a bean: it has no Spring dependencies, so it can be
 * unit tested directly, and it fails fast at startup rather than surfacing bad data per request.
 */
public class DatasetLoader {

    private static final Logger log = LoggerFactory.getLogger(DatasetLoader.class);

    private final ObjectMapper objectMapper;

    public DatasetLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Dataset load(DatasetProperties properties) {
        List<Store> stores = read(properties.stores(), Store.class);
        List<Transaction> transactions = read(properties.transactions(), Transaction.class);
        List<Incident> incidents = read(properties.incidents(), Incident.class);

        requireUniqueIds(stores, Store::storeId, "store");
        requireUniqueIds(transactions, Transaction::transactionId, "transaction");
        requireUniqueIds(incidents, Incident::incidentId, "incident");

        Set<String> knownStores = stores.stream().map(Store::storeId).collect(Collectors.toSet());
        warnOnOrphans(transactions, Transaction::storeId, knownStores, "transaction");
        warnOnOrphans(incidents, Incident::storeId, knownStores, "incident");

        log.info("Loaded dataset: {} stores, {} transactions, {} incidents",
                stores.size(), transactions.size(), incidents.size());
        return new Dataset(stores, transactions, incidents);
    }

    private <T> List<T> read(Resource resource, Class<T> elementType) {
        if (resource == null || !resource.exists()) {
            throw new DatasetLoadException("Data file not found: " + describe(resource));
        }
        try (InputStream in = resource.getInputStream()) {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
            List<T> records = objectMapper.readValue(in, listType);
            if (records == null || records.isEmpty()) {
                throw new DatasetLoadException("Data file contains no records: " + describe(resource));
            }
            return records;
        } catch (IOException e) {
            throw new DatasetLoadException("Failed to parse data file: " + describe(resource), e);
        }
    }

    private <T> void requireUniqueIds(List<T> records, Function<T, String> idExtractor, String label) {
        Set<String> seen = new HashSet<>(records.size());
        for (T record : records) {
            String id = idExtractor.apply(record);
            if (!seen.add(id)) {
                throw new DatasetLoadException("Duplicate %s id in source data: %s".formatted(label, id));
            }
        }
    }

    /** Referential gaps are tolerated but surfaced: the feed is a snapshot, not a transactional export. */
    private <T> void warnOnOrphans(List<T> records, Function<T, String> storeIdExtractor,
                                   Set<String> knownStores, String label) {
        long orphans = records.stream().map(storeIdExtractor).filter(id -> !knownStores.contains(id)).count();
        if (orphans > 0) {
            log.warn("{} {} record(s) reference a store that is absent from the store feed", orphans, label);
        }
    }

    private String describe(Resource resource) {
        return resource == null ? "<unset>" : resource.getDescription();
    }
}
