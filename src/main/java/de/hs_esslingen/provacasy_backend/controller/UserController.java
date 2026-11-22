
package de.hs_esslingen.provacasy_backend.controller;

import java.util.List;

import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.hs_esslingen.provacasy_backend.api.UsersApi;
import de.hs_esslingen.provacasy_backend.dto.Role;
import de.hs_esslingen.provacasy_backend.dto.User;
import de.hs_esslingen.provacasy_backend.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController implements UsersApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<User> getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No authenticated user"
            );
        }

        // Benutzer in PostgreSQL anlegen / aktualisieren
        de.hs_esslingen.provacasy_backend.model.User savedUser =
                userService.syncCurrentUser(jwtAuth.getToken());

        // Fachrolle bestimmen
        Role role = resolveRole(authentication);

        // Datenbank-Entity in OpenAPI-DTO umwandeln
        User dto = new User();

        dto.setId(savedUser.getId());
        dto.setFirstName(savedUser.getFirstName());
        dto.setLastName(savedUser.getLastName());
        dto.setEmail(savedUser.getEmail());
        dto.setRole(role);

        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<List<User>> searchUsers(String search) {
        // Wird in einem späteren Schritt implementiert
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "User search is not implemented yet"
        );
    }

    private Role resolveRole(Authentication authentication) {

        if (hasRole(authentication, "ROLE_DEAN")) {
            return Role.DEAN;
        }

        if (hasRole(authentication, "ROLE_SECRETARIAT")) {
            return Role.SECRETARIAT;
        }

        if (hasRole(authentication, "ROLE_PROFESSOR")) {
            return Role.PROFESSOR;
        }

        return null;
    }

    private boolean hasRole(
            Authentication authentication,
            String role
    ) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(role));
    }
}
