package com.company.adminbackend.config;

import com.company.adminbackend.security.JwtRoleConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Central security configuration for AdminAppBackend.
 *
 * Design decisions:
 * - STATELESS session: every request carries its own JWT — no server-side session needed.
 * - CSRF disabled: safe because we never use cookie-based auth (Bearer tokens only).
 * - URL-level rule: /admin/** requires authentication. Fine-grained role checks are
 *   enforced via @PreAuthorize at the method level (enabled by @EnableMethodSecurity).
 * - Audience validation: Entra ID tokens must include "adminapp-backend" in the "aud"
 *   claim. This prevents tokens meant for other APIs from being accepted here.
 * - OAuth2AuthorizedClientManager bean: wired for the client-credentials flow used by
 *   ServiceTokenProvider. AuthorizedClientServiceOAuth2AuthorizedClientManager is the
 *   correct choice for non-reactive (servlet) applications.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.audiences}")
    private String expectedAudience;

    // --- Security filter chain ---

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Health/readiness probes are public so load-balancers can check without a token.
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // All admin endpoints require an authenticated (valid JWT) user.
                .requestMatchers("/admin/**").authenticated()
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    // --- JWT decoder with audience validation ---

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuerUri);

        // Combine the default issuer/timestamp validators with a custom audience check.
        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(expectedAudience);
        OAuth2TokenValidator<Jwt> combined = new DelegatingOAuth2TokenValidator<>(defaultValidators, audienceValidator);

        decoder.setJwtValidator(combined);
        return decoder;
    }

    // --- JWT → Spring Security authorities mapping ---

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter());
        return converter;
    }

    // --- OAuth2 client manager for client-credentials flow ---

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        // AuthorizedClientServiceOAuth2AuthorizedClientManager is designed for
        // service-to-service use outside a servlet request context.
        return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService
        );
    }
}
