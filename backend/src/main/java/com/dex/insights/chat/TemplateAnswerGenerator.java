package com.dex.insights.chat;

import com.dex.insights.domain.Incident;
import com.dex.insights.domain.Store;
import com.dex.insights.domain.Tank;
import com.dex.insights.repository.IncidentRepository;
import com.dex.insights.repository.StoreRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deterministic answer composition. Runs with no credentials and is fully reproducible, which makes
 * it both the default and the fallback when an LLM call fails.
 *
 * <p>Grounding is structural rather than prompted: the question only selects which facts to report,
 * and every figure is read from the domain records behind the retrieved citations.
 */
@Component
public class TemplateAnswerGenerator implements AnswerGenerator {

    private static final int MAX_STORES = 3;
    private static final int MAX_INCIDENTS = 5;

    /** Question keywords mapped to the facts worth reporting. */
    private static final Map<Intent, Set<String>> INTENT_KEYWORDS = Map.of(
            Intent.OFFLINE_PUMPS, Set.of("offline", "pump", "down", "outage", "dispenser", "unavailable"),
            Intent.TANK, Set.of("tank", "fuel", "level", "runout", "empty", "gallon", "low", "ullage"),
            Intent.INCIDENTS, Set.of("incident", "issue", "alert", "severity", "problem", "outage", "fault"),
            Intent.HEALTH, Set.of("health", "healthy", "summarize", "summary", "status", "activity",
                    "overview", "recent"));

    private final StoreRepository storeRepository;
    private final IncidentRepository incidentRepository;

    public TemplateAnswerGenerator(StoreRepository storeRepository, IncidentRepository incidentRepository) {
        this.storeRepository = storeRepository;
        this.incidentRepository = incidentRepository;
    }

    private enum Intent {
        OFFLINE_PUMPS,
        TANK,
        INCIDENTS,
        HEALTH
    }

    @Override
    public String generate(ChatRequest request, AssembledContext context) {
        if (context.isEmpty()) {
            return "No records in the dataset match that question, so there is nothing to report. "
                    + "Try naming a store id, or asking about pump availability, tank levels or incidents.";
        }

        Set<Intent> intents = detectIntents(request.question());
        List<Store> stores = rank(context.storeIds().stream()
                .map(storeRepository::findById)
                .flatMap(Optional::stream)
                .toList(), intents);

        List<String> sections = new ArrayList<>();
        lead(stores, intents).ifPresent(sections::add);
        for (Store store : stores) {
            sections.add(describeStore(store, intents));
        }
        if (intents.contains(Intent.INCIDENTS) || intents.contains(Intent.HEALTH) || intents.isEmpty()) {
            String incidents = describeIncidents(stores);
            if (!incidents.isBlank()) {
                sections.add(incidents);
            }
        }
        if (sections.isEmpty()) {
            sections.add("The retrieved records do not contain enough detail to answer that directly.");
        }

        sections.add("Answer derived from %d cited dataset record(s)."
                .formatted(context.citations().size()));
        return String.join("\n\n", sections);
    }

    /**
     * Orders the retrieved stores by whatever the question is ranking on.
     * Lexical retrieval decides which stores are relevant; this decides how they are presented, so
     * a "highest offline pumps" question leads with the worst store even when retrieval scored
     * another one higher on wording alone.
     */
    private List<Store> rank(List<Store> stores, Set<Intent> intents) {
        Comparator<Store> order;
        if (intents.contains(Intent.OFFLINE_PUMPS)) {
            order = Comparator.comparingInt(Store::offlinePumps).reversed();
        } else if (intents.contains(Intent.TANK)) {
            order = Comparator.comparingDouble(store -> store.lowestTank().map(Tank::fillRatio).orElse(1d));
        } else {
            return stores.stream().limit(MAX_STORES).toList();
        }
        return stores.stream().sorted(order.thenComparing(Store::storeId)).limit(MAX_STORES).toList();
    }

    private Optional<String> lead(List<Store> stores, Set<Intent> intents) {
        if (stores.isEmpty()) {
            return Optional.empty();
        }
        if (intents.contains(Intent.OFFLINE_PUMPS)) {
            return Optional.of("Ranked by offline pumps across the retrieved records, worst first:");
        }
        if (intents.contains(Intent.TANK)) {
            long atRisk = stores.stream()
                    .filter(store -> store.lowestTank().map(tank -> tank.fillRatio() <= 0.25d).orElse(false))
                    .count();
            return Optional.of(atRisk == 0
                    ? "None of the retrieved stores has a tank at or below 25% of capacity."
                    : "%d of the retrieved store(s) have a tank at or below 25%% of capacity, emptiest first:"
                            .formatted(atRisk));
        }
        return Optional.empty();
    }

    private String describeStore(Store store, Set<Intent> intents) {
        StringBuilder text = new StringBuilder();
        String location = store.storeAddress() == null ? "an unknown location" : store.storeAddress().label();
        text.append("Store %s (%s, %s) is %s with %d of %d pumps offline"
                .formatted(store.storeId(), store.brand(), location, store.status(),
                        store.offlinePumps(), store.totalPumps()));

        if (store.offlinePumps() == 0) {
            text.append(" — all pumps are dispensing");
        }
        text.append(". Pump availability is %.0f%% and the anomaly count is %d."
                .formatted(store.pumpAvailability() * 100, store.anomalyCount()));

        if (store.hyperCare()) {
            text.append(" The store is flagged for hyperCare.");
        }

        boolean wantsTanks = intents.contains(Intent.TANK) || intents.contains(Intent.HEALTH) || intents.isEmpty();
        if (wantsTanks && !store.tanks().isEmpty()) {
            text.append(' ').append(describeTanks(store));
        }
        return text.toString();
    }

    private String describeTanks(Store store) {
        String readings = store.tanks().stream()
                .sorted(Comparator.comparingDouble(Tank::fillRatio))
                .map(tank -> "%s at %d of %d gallons (%.0f%%)".formatted(tank.gradeName(), tank.levelGallons(),
                        tank.capacityGallons(), tank.fillRatio() * 100))
                .collect(Collectors.joining("; "));

        Optional<Tank> lowest = store.lowestTank();
        String risk = lowest.filter(tank -> tank.fillRatio() <= 0.25d)
                .map(tank -> " The %s tank is below 25%% of capacity and is a runout risk."
                        .formatted(tank.gradeName()))
                .orElse("");
        return "Tank levels: %s.%s".formatted(readings, risk);
    }

    private String describeIncidents(List<Store> stores) {
        List<Incident> incidents = stores.stream()
                .flatMap(store -> incidentRepository.findByStoreId(store.storeId()).stream())
                .sorted(Comparator.comparingInt((Incident incident) ->
                                incident.severity() == null ? 0 : incident.severity().weight())
                        .reversed()
                        .thenComparing(Incident::timestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(MAX_INCIDENTS)
                .toList();

        if (incidents.isEmpty()) {
            return stores.isEmpty() ? "" : "No incidents are recorded against these stores.";
        }

        String lines = incidents.stream()
                .map(incident -> "- %s (store %s): %s severity %s incident, status %s — %s"
                        .formatted(incident.incidentId(), incident.storeId(), incident.severity(),
                                incident.category(), incident.status(), incident.description()))
                .collect(Collectors.joining("\n"));
        return "Associated incidents:\n" + lines;
    }

    private Set<Intent> detectIntents(String question) {
        Set<String> tokens = Set.copyOf(Tokenizer.tokenize(question.toLowerCase(Locale.ROOT)));
        return INTENT_KEYWORDS.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(tokens::contains))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }
}
