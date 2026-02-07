package com.company.adminbackend.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Writes structured audit events as JSON to the application log via SLF4J.
 *
 * Design decisions:
 * - Uses a dedicated logger named "AUDIT" so ops teams can route audit lines to a
 *   separate log sink (e.g., Splunk index, CloudWatch log group) via logback config.
 * - ObjectMapper is configured once at construction to avoid repeated setup.
 * - JavaTimeModule ensures Instant fields serialize as ISO-8601 strings, not epoch longs.
 * - If JSON serialization somehow fails, the event is logged in toString() form to
 *   avoid silently dropping audit records.
 */
@Service
public class AuditService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");
    private final ObjectMapper objectMapper;

    public AuditService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void log(AuditEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            auditLog.info("AUDIT_EVENT {}", json);
        } catch (JsonProcessingException e) {
            // Fallback: never lose an audit record even if serialization fails.
            auditLog.error("Failed to serialize audit event, raw: {}", event, e);
        }
    }
}
