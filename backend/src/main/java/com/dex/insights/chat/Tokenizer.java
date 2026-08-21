package com.dex.insights.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Lowercase word tokenisation with a small stop-word list.
 * Deliberately minimal: no stemming or embeddings, which would add dependencies and opacity for
 * little gain on a corpus of this size.
 */
final class Tokenizer {

    private static final Pattern SPLIT = Pattern.compile("[^a-z0-9]+");

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "any", "are", "as", "at", "be", "by", "do", "for", "from", "has", "have",
            "how", "in", "is", "it", "its", "me", "of", "on", "or", "our", "show", "so", "that", "the",
            "their", "them", "there", "these", "they", "this", "to", "was", "were", "what", "when",
            "which", "who", "why", "with", "you", "your");

    private Tokenizer() {
    }

    /**
     * Crude singularisation so "pumps" matches "pump" and "incidents" matches "incident".
     * Applied identically to documents and queries, so consistency matters more than linguistic
     * correctness; a real stemmer would be the next step if the corpus grew.
     */
    private static String normalize(String token) {
        return token.length() > 3 && token.endsWith("s") && !token.endsWith("ss") && !token.endsWith("us")
                ? token.substring(0, token.length() - 1)
                : token;
    }

    static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        for (String candidate : SPLIT.split(text.toLowerCase(Locale.ROOT))) {
            if (candidate.length() >= 2 && !STOP_WORDS.contains(candidate)) {
                tokens.add(normalize(candidate));
            }
        }
        return tokens;
    }
}
