package com.dex.insights.chat;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Turns ranked documents into a bounded context block plus the citations that describe it.
 *
 * <p>Citations are emitted here, from the same loop that builds the context, so an answer can never
 * cite a record that was not in the context it was generated from.
 */
@Component
public class ContextAssembler {

    private final ChatProperties properties;

    public ContextAssembler(ChatProperties properties) {
        this.properties = properties;
    }

    public AssembledContext assemble(List<ScoredDocument> retrieved) {
        List<ScoredDocument> included = new ArrayList<>();
        List<Citation> citations = new ArrayList<>();
        StringBuilder context = new StringBuilder();

        for (ScoredDocument scored : retrieved) {
            RetrievableDocument document = scored.document();
            String block = "[%s] %s%n".formatted(document.id(), document.text());
            if (context.length() + block.length() > properties.maxContextChars() && !included.isEmpty()) {
                break;
            }
            context.append(block);
            included.add(scored);
            citations.add(new Citation(document.type(), document.id(), document.storeId(),
                    document.timestamp(), document.text(), round(scored.score())));
        }

        return new AssembledContext(context.toString(), List.copyOf(included), List.copyOf(citations),
                summarize(included));
    }

    private String summarize(List<ScoredDocument> included) {
        if (included.isEmpty()) {
            return "No dataset records matched the question.";
        }
        String byType = included.stream()
                .collect(Collectors.groupingBy(scored -> scored.document().type(), Collectors.counting()))
                .entrySet().stream()
                .map(entry -> "%d %s".formatted(entry.getValue(), entry.getKey().name().toLowerCase()))
                .collect(Collectors.joining(", "));
        String stores = included.stream()
                .map(scored -> scored.document().storeId())
                .distinct()
                .collect(Collectors.joining(", "));
        return "Retrieved %d record(s) (%s) covering store(s) %s.".formatted(included.size(), byType, stores);
    }

    private static double round(double value) {
        return Math.round(value * 1000d) / 1000d;
    }
}
