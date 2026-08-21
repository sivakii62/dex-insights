package com.dex.insights.domain;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** A completed fuel sale at a dispenser. Monetary and volume values arrive as strings in the feed. */
public record Transaction(
        String transactionId,
        String storeId,
        String gradeName,
        BigDecimal transactionAmnt,
        BigDecimal volume,
        int dispenserId,
        Instant transactionStartTime,
        Instant transactionEndTime) {

    public Transaction {
        Objects.requireNonNull(transactionId, "transactionId is required");
        Objects.requireNonNull(storeId, "storeId is required");
    }

    /** Wall-clock length of the fuelling event, or zero when either timestamp is missing. */
    public Duration duration() {
        return transactionStartTime == null || transactionEndTime == null
                ? Duration.ZERO
                : Duration.between(transactionStartTime, transactionEndTime);
    }
}
