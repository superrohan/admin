package com.company.adminbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Provides a pre-configured WebClient bean targeting the ControllerApp.
 *
 * Design decisions:
 * - Base URL is externalized so each environment (dev, staging, prod) can point to
 *   its own ControllerApp instance without code changes.
 * - The Bearer token is NOT set here; it is attached per-request by ControllerClient
 *   so that each call uses a fresh (or cached-but-valid) service token.
 */
@Configuration
public class WebClientConfig {

    @Value("${controller-app.base-url}")
    private String controllerAppBaseUrl;

    @Bean
    public WebClient controllerAppWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(controllerAppBaseUrl)
                .build();
    }
}
