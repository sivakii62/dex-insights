package com.dex.insights.domain;

import java.time.Instant;

/** A single fuel tank reading from the store's automatic tank gauge. */
public record Tank(
        String gradeName,
        int capacityGallons,
        int levelGallons,
        int ullageGallons,
        Instant lastUpdatedTime) {

    /**
     * Fraction of capacity currently held, in the range 0..1.
     * Returns 0 when capacity is unknown so callers never divide by zero.
     */
    public double fillRatio() {
        return capacityGallons <= 0 ? 0d : (double) levelGallons / capacityGallons;
    }
}
