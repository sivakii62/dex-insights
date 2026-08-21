package com.dex.insights.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** OpenAPI 3.1 description served at /v3/api-docs and rendered by Swagger UI at /swagger-ui. */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    public OpenAPI dexInsightsOpenApi(@Value("${server.port:8080}") int port) {
        return new OpenAPI()
                .info(new Info()
                        .title("Dex Insights API")
                        .version("v1")
                        .description("""
                                Store operations API over a read-only snapshot of store status, transaction \
                                and incident data. Provides fleet insights and a grounded question-answering \
                                endpoint that cites the records its answers were derived from.""")
                        .contact(new Contact().name("Dex Insights"))
                        .license(new License().name("Proprietary")))
                .servers(List.of(new Server()
                        .url("http://localhost:" + port)
                        .description("Local")));
    }
}
