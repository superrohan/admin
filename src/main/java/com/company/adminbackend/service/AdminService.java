package com.company.adminbackend.service;

import com.company.adminbackend.audit.AuditEvent;
import com.company.adminbackend.audit.AuditFilter;
import com.company.adminbackend.audit.AuditService;
import com.company.adminbackend.client.ControllerClient;
import com.company.adminbackend.model.ScanResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic layer that orchestrates admin operations.
 *
 * Design decisions:
 * - Delegates all HTTP communication to ControllerClient so this layer stays
 *   transport-agnostic and testable.
 * - Audit logging wraps the call: BEFORE (attempt), AFTER success, AFTER failure.
 *   This ensures a complete audit trail even if the downstream call throws.
 * - User context (username, userId, roles) is passed in from the controller rather
 *   than injecting SecurityContext here, keeping the service layer decoupled from
 *   the servlet/security stack.
 */
@Service
public class AdminService {

    private final ControllerClient controllerClient;
    private final AuditService auditService;

    public AdminService(ControllerClient controllerClient, AuditService auditService) {
        this.controllerClient = controllerClient;
        this.auditService = auditService;
    }

    public ScanResponse forceCloseScan(String scanId, String username, String userId, List<String> roles) {
        String correlationId = MDC.get(AuditFilter.CORRELATION_ID_KEY);

        // Audit: before call
        auditService.log(AuditEvent.builder()
                .action("FORCE_CLOSE_SCAN")
                .username(username)
                .userId(userId)
                .roles(roles)
                .resourceId(scanId)
                .status("ATTEMPT")
                .correlationId(correlationId)
                .build());

        try {
            ScanResponse response = controllerClient.forceCloseScan(scanId);

            // Audit: after success
            auditService.log(AuditEvent.builder()
                    .action("FORCE_CLOSE_SCAN")
                    .username(username)
                    .userId(userId)
                    .roles(roles)
                    .resourceId(scanId)
                    .status("SUCCESS")
                    .correlationId(correlationId)
                    .build());

            return response;

        } catch (Exception e) {
            // Audit: after failure
            auditService.log(AuditEvent.builder()
                    .action("FORCE_CLOSE_SCAN")
                    .username(username)
                    .userId(userId)
                    .roles(roles)
                    .resourceId(scanId)
                    .status("FAILURE")
                    .correlationId(correlationId)
                    .build());

            throw e;
        }
    }

    public ScanResponse getScan(String scanId, String username, String userId, List<String> roles) {
        String correlationId = MDC.get(AuditFilter.CORRELATION_ID_KEY);

        auditService.log(AuditEvent.builder()
                .action("GET_SCAN")
                .username(username)
                .userId(userId)
                .roles(roles)
                .resourceId(scanId)
                .status("ATTEMPT")
                .correlationId(correlationId)
                .build());

        try {
            ScanResponse response = controllerClient.getScan(scanId);

            auditService.log(AuditEvent.builder()
                    .action("GET_SCAN")
                    .username(username)
                    .userId(userId)
                    .roles(roles)
                    .resourceId(scanId)
                    .status("SUCCESS")
                    .correlationId(correlationId)
                    .build());

            return response;

        } catch (Exception e) {
            auditService.log(AuditEvent.builder()
                    .action("GET_SCAN")
                    .username(username)
                    .userId(userId)
                    .roles(roles)
                    .resourceId(scanId)
                    .status("FAILURE")
                    .correlationId(correlationId)
                    .build());

            throw e;
        }
    }
}
