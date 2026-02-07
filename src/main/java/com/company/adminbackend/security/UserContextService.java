package com.company.adminbackend.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Extracts user identity from the validated JWT carried by the current request.
 *
 * Design decisions:
 * - Receives JwtAuthenticationToken (already validated by the resource-server filter chain).
 * - "preferred_username" is the Entra ID claim for the user's email / UPN.
 * - "sub" is the immutable object-id that uniquely identifies the user.
 * - Roles come from the authorities already mapped by JwtRoleConverter;
 *   we strip the "ROLE_" prefix so callers get clean domain names (ADMIN, SUPPORT).
 */
@Service
public class UserContextService {

    private static final String ROLE_PREFIX = "ROLE_";

    public String getUsername(JwtAuthenticationToken token) {
        Object preferred = token.getTokenAttributes().get("preferred_username");
        return preferred != null ? preferred.toString() : token.getName();
    }

    public String getUserId(JwtAuthenticationToken token) {
        // "sub" is the standard OIDC subject claim — stable across sessions.
        return token.getToken().getSubject();
    }

    public List<String> getRoles(JwtAuthenticationToken token) {
        if (token.getAuthorities() == null) {
            return Collections.emptyList();
        }
        return token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(ROLE_PREFIX))
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .collect(Collectors.toUnmodifiableList());
    }
}
