package com.company.adminbackend.audit;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Structured audit event written as JSON to the application log.
 *
 * Design decisions:
 * - Immutable after construction via builder to prevent mutation after logging.
 * - JsonInclude(NON_NULL) keeps log lines compact when optional fields are absent.
 * - correlationId ties the event back to the originating HTTP request (set in MDC by AuditFilter).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditEvent {

    private final String action;
    private final String username;
    private final String userId;
    private final List<String> roles;
    private final String resourceId;
    private final Instant timestamp;
    private final String status;
    private final String correlationId;

    private AuditEvent(Builder builder) {
        this.action = builder.action;
        this.username = builder.username;
        this.userId = builder.userId;
        this.roles = builder.roles;
        this.resourceId = builder.resourceId;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.status = builder.status;
        this.correlationId = builder.correlationId;
    }

    public String getAction() {
        return action;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String action;
        private String username;
        private String userId;
        private List<String> roles;
        private String resourceId;
        private Instant timestamp;
        private String status;
        private String correlationId;

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public AuditEvent build() {
            return new AuditEvent(this);
        }
    }
}
