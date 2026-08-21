package com.dex.insights.domain;

/** Postal locality of a store. Only the fields present in the source feed are modelled. */
public record StoreAddress(String state, String city) {

    /** Human-readable "City, ST" label used in retrieval text and the UI. */
    public String label() {
        if (city == null && state == null) {
            return "Unknown location";
        }
        if (city == null) {
            return state;
        }
        return state == null ? city : city + ", " + state;
    }
}
