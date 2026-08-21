package com.dex.insights;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InsightsApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsightsApplication.class, args);
    }
}
