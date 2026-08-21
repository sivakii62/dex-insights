package com.dex.insights.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

/**
 * Locations of the source data files. Defaults point at the copies bundled in the jar; any of them
 * can be overridden with a {@code file:} URI to run against an externally mounted dataset.
 */
@Validated
@ConfigurationProperties(prefix = "dex.data")
public record DatasetProperties(
        @NotNull Resource stores,
        @NotNull Resource transactions,
        @NotNull Resource incidents) {
}
