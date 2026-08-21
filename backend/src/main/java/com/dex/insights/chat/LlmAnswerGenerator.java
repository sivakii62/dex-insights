package com.dex.insights.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Optional generation through any OpenAI-compatible chat completions endpoint (OpenAI, Ollama,
 * vLLM, Bedrock gateways). Active only when {@code dex.chat.llm.enabled=true}, so the application
 * runs with no credentials by default.
 *
 * <p>The prompt carries only retrieved context and forbids outside knowledge; any failure falls
 * back to the deterministic generator rather than surfacing an error to the caller.
 */
@Component
@Primary
@ConditionalOnProperty(prefix = "dex.chat.llm", name = "enabled", havingValue = "true")
public class LlmAnswerGenerator implements AnswerGenerator {

    private static final Logger log = LoggerFactory.getLogger(LlmAnswerGenerator.class);

    private static final String SYSTEM_PROMPT = """
            You are an assistant for retail fuel store operations. Answer only from the CONTEXT block.
            Every figure you state must appear in the context. If the context does not contain the
            answer, say so plainly. Do not speculate and do not use outside knowledge. Be concise.""";

    private final ChatProperties properties;
    private final TemplateAnswerGenerator fallback;
    private final RestClient restClient;

    public LlmAnswerGenerator(ChatProperties properties, TemplateAnswerGenerator fallback,
                              RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.fallback = fallback;
        this.restClient = restClientBuilder.baseUrl(properties.llm().baseUrl()).build();
        log.info("LLM answer generation enabled (model={}, baseUrl={})",
                properties.llm().model(), properties.llm().baseUrl());
    }

    @Override
    public String generate(ChatRequest request, AssembledContext context) {
        if (context.isEmpty()) {
            return fallback.generate(request, context);
        }
        try {
            return callModel(request, context);
        } catch (RuntimeException e) {
            log.warn("LLM generation failed, falling back to deterministic answer: {}", e.getMessage());
            return fallback.generate(request, context);
        }
    }

    private String callModel(ChatRequest request, AssembledContext context) {
        String userPrompt = """
                CONTEXT:
                %s
                QUESTION: %s""".formatted(context.contextText(), request.question());

        Map<String, Object> body = Map.of(
                "model", properties.llm().model(),
                "temperature", 0,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt)));

        CompletionResponse response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> {
                    if (StringUtils.hasText(properties.llm().apiKey())) {
                        headers.setBearerAuth(properties.llm().apiKey());
                    }
                })
                .body(body)
                .retrieve()
                .body(CompletionResponse.class);

        String answer = response == null || response.choices() == null || response.choices().isEmpty()
                ? null
                : response.choices().getFirst().message().content();

        if (!StringUtils.hasText(answer)) {
            throw new IllegalStateException("Model returned an empty completion");
        }
        return answer.strip();
    }

    private record CompletionResponse(List<Choice> choices) {
        private record Choice(Message message) {
        }

        private record Message(String content) {
        }
    }
}
