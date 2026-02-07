package com.company.adminbackend.controller;

import com.company.adminbackend.model.ScanResponse;
import com.company.adminbackend.security.UserContextService;
import com.company.adminbackend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for admin operations on scans.
 *
 * Design decisions:
 * - @PreAuthorize("hasRole('ADMIN')") enforces that only users with the ADMIN role
 *   (mapped from the JWT "roles" claim by JwtRoleConverter) can access these endpoints.
 *   This is a defense-in-depth layer on top of the URL-level .authenticated() rule
 *   in SecurityConfig.
 * - JwtAuthenticationToken is injected by Spring Security after successful JWT validation.
 *   UserContextService extracts user identity fields from it.
 * - Returns ResponseEntity so we have explicit control over status codes.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserContextService userContextService;

    public AdminController(AdminService adminService, UserContextService userContextService) {
        this.adminService = adminService;
        this.userContextService = userContextService;
    }

    @PostMapping("/scan/{scanId}/force-close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScanResponse> forceCloseScan(
            @PathVariable String scanId,
            JwtAuthenticationToken authentication) {

        String username = userContextService.getUsername(authentication);
        String userId = userContextService.getUserId(authentication);
        List<String> roles = userContextService.getRoles(authentication);

        ScanResponse response = adminService.forceCloseScan(scanId, username, userId, roles);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scan/{scanId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScanResponse> getScan(
            @PathVariable String scanId,
            JwtAuthenticationToken authentication) {

        String username = userContextService.getUsername(authentication);
        String userId = userContextService.getUserId(authentication);
        List<String> roles = userContextService.getRoles(authentication);

        ScanResponse response = adminService.getScan(scanId, username, userId, roles);
        return ResponseEntity.ok(response);
    }
}
