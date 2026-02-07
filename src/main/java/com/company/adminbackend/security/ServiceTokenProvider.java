package com.company.adminbackend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;

/**
 * Retrieves and caches an OAuth2 client-credentials token for calling ControllerApp.
 *
 * Design decisions:
 * - Delegates to Spring's OAuth2AuthorizedClientManager which handles:
 *     • token acquisition via the client-credentials grant
 *     • caching of tokens until they expire
 *     • automatic refresh when the cached token is expired
 *   This avoids manual token caching and TTL logic.
 * - The registration id "controller-app" matches application.yml.
 * - The principal name is a synthetic value ("admin-app-backend") because the
 *   client-credentials flow has no end-user principal.
 */
@Component
public class ServiceTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(ServiceTokenProvider.class);
    private static final String REGISTRATION_ID = "controller-app";
    private static final String PRINCIPAL_NAME = "admin-app-backend";

    private final OAuth2AuthorizedClientManager clientManager;

    public ServiceTokenProvider(OAuth2AuthorizedClientManager clientManager) {
        this.clientManager = clientManager;
    }

    /**
     * Returns a valid access token for the ControllerApp.
     * The underlying client manager caches the token and only re-acquires when expired.
     */
    public String getServiceToken() {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId(REGISTRATION_ID)
                .principal(PRINCIPAL_NAME)
                .build();

        OAuth2AuthorizedClient authorizedClient = clientManager.authorize(request);

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new IllegalStateException("Failed to obtain service token for ControllerApp");
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        log.debug("Service token obtained, expires at {}", accessToken.getExpiresAt());
        return accessToken.getTokenValue();
    }
}
