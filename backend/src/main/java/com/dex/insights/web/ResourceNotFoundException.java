package com.dex.insights.web;

/** Thrown when a requested record does not exist; mapped to a 404 problem response. */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String identifier;

    public ResourceNotFoundException(String resourceType, String identifier) {
        super("%s '%s' was not found".formatted(resourceType, identifier));
        this.resourceType = resourceType;
        this.identifier = identifier;
    }

    public String resourceType() {
        return resourceType;
    }

    public String identifier() {
        return identifier;
    }
}
