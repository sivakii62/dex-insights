package com.dex.insights.chat;

/**
 * Composes the answer text from retrieved context.
 *
 * <p>The seam that keeps LLM use optional: the deterministic implementation is always present, and
 * an LLM-backed one takes precedence only when explicitly configured.
 */
public interface AnswerGenerator {

    String generate(ChatRequest request, AssembledContext context);
}
