
package de.hs_esslingen.provacasy_backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakJwtAuthenticationConverterTest {

    private final KeycloakJwtAuthenticationConverter converter =
            new KeycloakJwtAuthenticationConverter();

    private Jwt createJwt(String group) {
        Instant now = Instant.now();

        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("test-user-id")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim("groups", List.of(group))
                .build();
    }

    private void assertRole(String group, String expectedRole) {
        Jwt jwt = createJwt(group);

        AbstractAuthenticationToken authentication =
                converter.convert(jwt);

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(expectedRole)));
    }

    @Test
    void professorShouldHaveProfessorRole() {
        assertRole("professors", "ROLE_PROFESSOR");
    }

    @Test
    void staffShouldHaveSecretariatRole() {
        assertRole("staff", "ROLE_SECRETARIAT");
    }

    @Test
    void dekanatShouldHaveDeanRole() {
        assertRole("dekanat", "ROLE_DEAN");
    }

    @Test
    void studentShouldNotHaveEmployeeRoles() {
        Jwt jwt = createJwt("students");

        AbstractAuthenticationToken authentication =
                converter.convert(jwt);

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .noneMatch(authority ->
                        authority.getAuthority().startsWith("ROLE_")));
    }

    @Test
    void jwtWithoutGroupsShouldNotHaveEmployeeRoles() {
        Instant now = Instant.now();

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("test-user-id")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();

        AbstractAuthenticationToken authentication =
                converter.convert(jwt);

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .noneMatch(authority ->
                        authority.getAuthority().startsWith("ROLE_")));
    }
}
