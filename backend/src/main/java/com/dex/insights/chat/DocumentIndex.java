package com.dex.insights.chat;

import com.dex.insights.domain.Incident;
import com.dex.insights.domain.Store;
import com.dex.insights.domain.Tank;
import com.dex.insights.domain.Transaction;
import com.dex.insights.repository.IncidentRepository;
import com.dex.insights.repository.StoreRepository;
import com.dex.insights.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Renders every dataset record as a searchable document once at startup.
 *
 * <p>The rendering matters more than the search algorithm: spelling out numbers and states in
 * natural language is what lets a keyword query like "low tank levels" reach the right records.
 */
@Component
public class DocumentIndex {

    private static final Logger log = LoggerFactory.getLogger(DocumentIndex.class);

    private final List<RetrievableDocument> documents;
    private final Map<String, List<RetrievableDocument>> incidentsByStore;

    public DocumentIndex(StoreRepository stores, IncidentRepository incidents, TransactionRepository transactions) {
        List<RetrievableDocument> all = new ArrayList<>();
        stores.findAll().forEach(store -> all.add(storeDocument(store)));
        incidents.findAll().forEach(incident -> all.add(incidentDocument(incident)));
        transactions.findAll().forEach(transaction -> all.add(transactionDocument(transaction)));
        this.documents = List.copyOf(all);
        this.incidentsByStore = documents.stream()
                .filter(document -> document.type() == DocumentType.INCIDENT)
                .collect(Collectors.groupingBy(RetrievableDocument::storeId));
        log.info("Indexed {} retrievable documents", documents.size());
    }

    public List<RetrievableDocument> documents() {
        return documents;
    }

    /** Incident documents linked to a store, used to expand context around a retrieved store. */
    public List<RetrievableDocument> incidentsForStore(String storeId) {
        return incidentsByStore.getOrDefault(storeId, List.of());
    }

    private RetrievableDocument storeDocument(Store store) {
        String location = store.storeAddress() == null ? "unknown location" : store.storeAddress().label();
        String tanks = store.tanks().isEmpty()
                ? "No tank gauge readings."
                : store.tanks().stream().map(DocumentIndex::describeTank).collect(Collectors.joining(" "));

        String text = """
                Store %s, brand %s, located in %s. Operational status %s. \
                %d of %d pumps are offline and %d pumps are active (%.0f%% pump availability). \
                Anomaly count %d. HyperCare %s. Last updated %s. %s"""
                .formatted(store.storeId(), store.brand(), location, store.status(),
                        store.offlinePumps(), store.totalPumps(), store.activePumps(),
                        store.pumpAvailability() * 100, store.anomalyCount(),
                        store.hyperCare() ? "enabled" : "disabled",
                        store.lastUpdatedTime(), tanks);

        return document(store.storeId(), DocumentType.STORE, store.storeId(),
                "Store %s (%s)".formatted(store.storeId(), store.brand()), text, store.lastUpdatedTime());
    }

    private static String describeTank(Tank tank) {
        return "Tank %s holds %d of %d gallons (%.0f%% full, ullage %d gallons)."
                .formatted(tank.gradeName(), tank.levelGallons(), tank.capacityGallons(),
                        tank.fillRatio() * 100, tank.ullageGallons());
    }

    private RetrievableDocument incidentDocument(Incident incident) {
        String text = "Incident %s at store %s. Severity %s. Category %s. Status %s. Raised %s. %s"
                .formatted(incident.incidentId(), incident.storeId(), incident.severity(),
                        incident.category(), incident.status(), incident.timestamp(), incident.description());

        return document(incident.incidentId(), DocumentType.INCIDENT, incident.storeId(),
                "Incident %s".formatted(incident.incidentId()), text, incident.timestamp());
    }

    private RetrievableDocument transactionDocument(Transaction transaction) {
        String text = "Transaction %s at store %s on dispenser %d dispensed %s gallons of %s for %s dollars between %s and %s."
                .formatted(transaction.transactionId(), transaction.storeId(), transaction.dispenserId(),
                        transaction.volume(), transaction.gradeName(), transaction.transactionAmnt(),
                        transaction.transactionStartTime(), transaction.transactionEndTime());

        return document(transaction.transactionId(), DocumentType.TRANSACTION, transaction.storeId(),
                "Transaction %s".formatted(transaction.transactionId()), text, transaction.transactionStartTime());
    }

    private RetrievableDocument document(String id, DocumentType type, String storeId, String title,
                                         String text, java.time.Instant timestamp) {
        return new RetrievableDocument(id, type, storeId, title, text, timestamp, Tokenizer.tokenize(text));
    }
}
