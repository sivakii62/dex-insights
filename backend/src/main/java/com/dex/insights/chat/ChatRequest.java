package com.dex.insights.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param question the natural-language question to answer from dataset context
 * @param storeId  optional scope; when present, retrieval is restricted to that store's records
 */
public record ChatRequest(
        @NotBlank(message = "question must not be blank")
        @Size(max = 500, message = "question must be at most 500 characters")
        @Schema(example = "Which stores have the highest offline pumps and what incidents are associated with them?")
        String question,

        @Size(max = 32)
        @Schema(example = "10001")
        String storeId) {
}
