package com.dex.insights.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Shared infrastructure beans. */
@Configuration(proxyBeanMethods = false)
public class ApplicationConfig {

    /** Injected rather than called statically so time-dependent output is testable. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
