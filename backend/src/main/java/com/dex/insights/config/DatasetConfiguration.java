package com.dex.insights.config;

import com.dex.insights.domain.Dataset;
import com.dex.insights.repository.DatasetLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the one-shot dataset load into the application context. */
@Configuration(proxyBeanMethods = false)
public class DatasetConfiguration {

    @Bean
    public Dataset dataset(DatasetProperties properties, ObjectMapper objectMapper) {
        return new DatasetLoader(objectMapper).load(properties);
    }
}
