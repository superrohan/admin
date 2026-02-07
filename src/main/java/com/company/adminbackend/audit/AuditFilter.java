package com.company.adminbackend.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that assigns a unique correlationId to every inbound request.
 *
 * Design decisions:
 * - Runs early in the filter chain (Ordered.HIGHEST_PRECEDENCE + 10) so that the
 *   correlationId is available to all downstream filters, controllers, and services.
 * - Stores the correlationId in SLF4J MDC so every log line within the request
 *   automatically includes it (pattern: [%X{correlationId}]).
 * - Also sets the correlationId as a response header so the caller can correlate
 *   client-side logs with server-side audit events.
 * - MDC is cleared in a finally block to prevent leaking to the next request on the
 *   same thread (important for thread-pool reuse).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuditFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_KEY = "correlationId";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Reuse a correlation id from the caller if present; otherwise generate one.
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}
