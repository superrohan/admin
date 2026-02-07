package com.company.adminbackend.client;

import com.company.adminbackend.model.ScanResponse;
import com.company.adminbackend.security.ServiceTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

/**
 * HTTP client for calling ControllerApp endpoints.
 *
 * Design decisions:
 * - Uses WebClient (non-blocking) instead of RestTemplate which is in maintenance mode.
 * - Each call fetches the service token from ServiceTokenProvider, which internally
 *   caches the token and only refreshes when expired.
 * - .block() converts the reactive Mono to a synchronous result. This is acceptable
 *   because AdminAppBackend is a servlet-stack application; if the service later
 *   migrates to WebFlux, .block() can be removed.
 * - Error responses from ControllerApp are mapped to ResponseStatusException so they
 *   propagate as proper HTTP errors to the AdminApp caller.
 */
@Component
public class ControllerClient {

    private static final Logger log = LoggerFactory.getLogger(ControllerClient.class);

    private final WebClient webClient;
    private final ServiceTokenProvider serviceTokenProvider;

    public ControllerClient(WebClient controllerAppWebClient, ServiceTokenProvider serviceTokenProvider) {
        this.webClient = controllerAppWebClient;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    /**
     * Calls ControllerApp to force-close the specified scan.
     */
    public ScanResponse forceCloseScan(String scanId) {
        log.debug("Calling ControllerApp to force-close scan {}", scanId);
        return webClient.post()
                .uri("/api/scan/{scanId}/force-close", scanId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceTokenProvider.getServiceToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                    clientResponse.bodyToMono(String.class)
                        .map(body -> new ResponseStatusException(
                                clientResponse.statusCode(),
                                "ControllerApp error: " + body
                        ))
                )
                .bodyToMono(ScanResponse.class)
                .block();
    }

    /**
     * Calls ControllerApp to retrieve scan details.
     */
    public ScanResponse getScan(String scanId) {
        log.debug("Calling ControllerApp to get scan {}", scanId);
        return webClient.get()
                .uri("/api/scan/{scanId}", scanId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceTokenProvider.getServiceToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                    clientResponse.bodyToMono(String.class)
                        .map(body -> new ResponseStatusException(
                                clientResponse.statusCode(),
                                "ControllerApp error: " + body
                        ))
                )
                .bodyToMono(ScanResponse.class)
                .block();
    }
}
