package com.dex.insights.domain;

/** Incident severity, ordered so that the most urgent sorts first. */
public enum IncidentSeverity {
    HIGH(3),
    MEDIUM(2),
    LOW(1);

    private final int weight;

    IncidentSeverity(int weight) {
        this.weight = weight;
    }

    /** Relative urgency; higher is more severe. */
    public int weight() {
        return weight;
    }
}
