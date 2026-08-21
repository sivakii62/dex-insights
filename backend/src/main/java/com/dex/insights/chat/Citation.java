package com.dex.insights.chat;

import java.time.Instant;

/**
 * A reference to the dataset record an answer was derived from.
 *
 * @param recordId  the identifier in the source data (store id, incident id, transaction id)
 * @param snippet   the exact retrieved text, so a reader can verify the claim without a second call
 */
public record Citation(
        DocumentType type,
        String recordId,
        String storeId,
        Instant timestamp,
        String snippet,
        double relevance) {
}
