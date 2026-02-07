package com.company.adminbackend.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts the "roles" claim in the Entra ID JWT into Spring Security GrantedAuthority objects.
 *
 * Design decisions:
 * - Entra ID places app roles in the "roles" claim (a JSON array of strings).
 * - Spring Security's hasRole("ADMIN") checks for authority "ROLE_ADMIN",
 *   so we prefix each role with "ROLE_".
 * - If the claim is missing the user has no roles, which effectively denies
 *   access to any role-protected endpoint.
 */
public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .collect(Collectors.toUnmodifiableList());
    }
}
