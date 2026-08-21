package com.dex.insights.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** Browser origins permitted to call the API; the UI dev server is the default. */
@ConfigurationProperties(prefix = "dex.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }
}
