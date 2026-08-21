package com.dex.insights.domain;

/** Lifecycle state of an incident. */
public enum IncidentStatus {
    OPEN,
    ACKNOWLEDGED,
    RESOLVED;

    /** An incident still requiring operator attention. */
    public boolean isActive() {
        return this != RESOLVED;
    }
}
