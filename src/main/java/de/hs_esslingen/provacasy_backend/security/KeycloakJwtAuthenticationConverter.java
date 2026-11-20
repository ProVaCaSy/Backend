
package de.hs_esslingen.provacasy_backend.security;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class KeycloakJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        Set<String> groups = extractGroups(jwt);

        String role = resolveRole(groups);

        List<SimpleGrantedAuthority> authorities =
                role == null
                    ? List.of()
                    : List.of(
                        new SimpleGrantedAuthority("ROLE_" + role)
                    );

        return new JwtAuthenticationToken(
                jwt,
                authorities,
                jwt.getSubject()
        );
    }

    private Set<String> extractGroups(Jwt jwt) {

        Object claim = jwt.getClaim("groups");

        if (!(claim instanceof Collection<?> values)) {
            return Set.of();
        }

        return values.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(group -> group.substring(
                        group.lastIndexOf('/') + 1
                ))
                .collect(Collectors.toSet());
    }

    private String resolveRole(Set<String> groups) {

        if (groups.contains("dekanat")) {
            return "DEAN";
        }

        if (groups.contains("staff")) {
            return "SECRETARIAT";
        }

        if (groups.contains("professors")) {
            return "PROFESSOR";
        }

        return null;
    }
}
