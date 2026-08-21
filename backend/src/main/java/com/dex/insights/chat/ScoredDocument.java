package com.dex.insights.chat;

/** A retrieved document together with its relevance score. */
public record ScoredDocument(RetrievableDocument document, double score) {
}
