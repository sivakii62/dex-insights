package com.dex.insights.chat;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Retrieval and generation settings.
 *
 * @param topK             number of documents assembled into the answer context
 * @param maxContextChars  upper bound on assembled context, so prompts stay bounded as data grows
 * @param llm              optional LLM generation; disabled by default so the app runs with no credentials
 */
@ConfigurationProperties(prefix = "dex.chat")
public record ChatProperties(int topK, int maxContextChars, Llm llm) {

    public ChatProperties {
        topK = topK <= 0 ? 6 : topK;
        maxContextChars = maxContextChars <= 0 ? 4000 : maxContextChars;
        llm = llm == null ? new Llm(false, null, null, null, null) : llm;
    }

    public record Llm(boolean enabled, String baseUrl, String apiKey, String model, Duration timeout) {

        public Llm {
            baseUrl = baseUrl == null ? "https://api.openai.com/v1" : baseUrl;
            model = model == null ? "gpt-4o-mini" : model;
            timeout = timeout == null ? Duration.ofSeconds(20) : timeout;
        }
    }
}
