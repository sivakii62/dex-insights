package com.dex.insights.chat;

import java.time.Instant;
import java.util.List;

/**
 * One indexed unit of dataset context.
 *
 * @param id     the source record identifier, used verbatim in citations
 * @param text   the searchable rendering of the record
 * @param tokens pre-tokenised {@code text}, computed once at index time
 */
public record RetrievableDocument(
        String id,
        DocumentType type,
        String storeId,
        String title,
        String text,
        Instant timestamp,
        List<String> tokens) {

    public RetrievableDocument {
        tokens = List.copyOf(tokens);
    }
}
