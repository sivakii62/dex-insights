package com.dex.insights.chat;

import java.util.List;
import java.util.Objects;

/**
 * The retrieved context an answer must be derived from.
 *
 * @param contextText the concatenated document text, truncated to the configured budget
 * @param citations   one citation per document included in {@code contextText}
 * @param summary     a short description of what retrieval selected
 */
public record AssembledContext(String contextText, List<ScoredDocument> documents,
                               List<Citation> citations, String summary) {

    public boolean isEmpty() {
        return documents.isEmpty();
    }

    /** Distinct store ids present in the retrieved context, in relevance order. */
    public List<String> storeIds() {
        return documents.stream()
                .map(scored -> scored.document().storeId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
