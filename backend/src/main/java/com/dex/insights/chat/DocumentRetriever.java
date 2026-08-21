package com.dex.insights.chat;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BM25 keyword retrieval over the in-memory document index.
 *
 * <p>BM25 rather than plain TF-IDF because it damps term saturation and normalises for document
 * length, which matters here: store documents are much longer than transaction documents and would
 * otherwise dominate every query. An explicit store-id boost handles the common
 * "tell me about store 10001" phrasing, where lexical overlap alone is a weak signal.
 */
@Component
public class DocumentRetriever {

    private static final double K1 = 1.2d;
    private static final double B = 0.75d;
    private static final double STORE_ID_BOOST = 6.0d;

    private final List<RetrievableDocument> documents;
    private final Map<String, Integer> documentFrequency;
    private final double averageLength;

    public DocumentRetriever(DocumentIndex index) {
        this.documents = index.documents();
        this.documentFrequency = new HashMap<>();
        for (RetrievableDocument document : documents) {
            document.tokens().stream().distinct()
                    .forEach(token -> documentFrequency.merge(token, 1, Integer::sum));
        }
        this.averageLength = documents.stream().mapToInt(d -> d.tokens().size()).average().orElse(1d);
    }

    /**
     * @param storeId when set, only that store's records are considered
     * @param topK    maximum number of documents returned
     */
    public List<ScoredDocument> retrieve(String question, String storeId, int topK) {
        List<String> queryTokens = Tokenizer.tokenize(question);
        List<String> mentionedStoreIds = extractStoreIds(queryTokens);

        List<ScoredDocument> scored = new ArrayList<>();
        for (RetrievableDocument document : documents) {
            if (StringUtils.hasText(storeId) && !storeId.equals(document.storeId())) {
                continue;
            }
            double score = bm25(queryTokens, document);
            if (mentionedStoreIds.contains(document.storeId())) {
                score += STORE_ID_BOOST;
            }
            if (score > 0) {
                scored.add(new ScoredDocument(document, score));
            }
        }

        scored.sort(Comparator.comparingDouble(ScoredDocument::score).reversed()
                .thenComparing(scoredDocument -> scoredDocument.document().id()));
        return scored.size() <= topK ? List.copyOf(scored) : List.copyOf(scored.subList(0, topK));
    }

    private double bm25(List<String> queryTokens, RetrievableDocument document) {
        int length = document.tokens().size();
        double score = 0d;
        for (String term : queryTokens.stream().distinct().toList()) {
            long frequency = document.tokens().stream().filter(term::equals).count();
            if (frequency == 0) {
                continue;
            }
            double idf = idf(term);
            score += idf * (frequency * (K1 + 1)) / (frequency + K1 * (1 - B + B * length / averageLength));
        }
        return score;
    }

    private double idf(String term) {
        int matching = documentFrequency.getOrDefault(term, 0);
        return Math.log(1 + (documents.size() - matching + 0.5d) / (matching + 0.5d));
    }

    /** Bare numeric tokens in a question are almost always store ids in this domain. */
    private List<String> extractStoreIds(List<String> queryTokens) {
        return queryTokens.stream().filter(token -> token.chars().allMatch(Character::isDigit)).toList();
    }
}
