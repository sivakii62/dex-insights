package com.dex.insights.chat;

import java.util.List;

/**
 * @param answer                   grounded answer, derived only from the retrieved records
 * @param citations                the records the answer was built from
 * @param retrievedContextSummary  what retrieval selected and why, for transparency and debugging
 */
public record ChatResponse(
        String answer,
        List<Citation> citations,
        String retrievedContextSummary) {
}
