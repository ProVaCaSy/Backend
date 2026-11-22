
package de.hs_esslingen.provacasy_backend.service;

import de.hs_esslingen.provacasy_backend.model.User;
import de.hs_esslingen.provacasy_backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    private Jwt createJwt(String firstName, String lastName, String email) {
        Instant now = Instant.now();

        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim("given_name", firstName)
                .claim("family_name", lastName)
                .claim("email", email)
                .build();
    }

    @Test
    void newUserShouldBeCreated() {
        Jwt jwt = createJwt("Max", "Mustermann", "max@example.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.syncCurrentUser(jwt);

        assertEquals(userId, result.getId());
        assertEquals("Max", result.getFirstName());
        assertEquals("Mustermann", result.getLastName());
        assertEquals("max@example.com", result.getEmail());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void existingUserShouldBeUpdated() {
        User existingUser = new User(
                userId, "Old", "Name", "old@example.com"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Jwt jwt = createJwt("New", "Name", "new@example.com");

        User result = userService.syncCurrentUser(jwt);

        assertSame(existingUser, result);
        assertEquals("New", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    void existingUserShouldNotBeCreatedAgain() {
        User existingUser = new User(
                userId, "Max", "Mustermann", "max@example.com"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.syncCurrentUser(
                createJwt("Max", "Mustermann", "max@example.com")
        );

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository, times(1)).save(captor.capture());

        assertSame(existingUser, captor.getValue());
    }

    @Test
    void missingEmailShouldThrowException() {
        Jwt jwt = createJwt("Max", "Mustermann", "");

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> userService.syncCurrentUser(jwt)
                );

        assertEquals(
                "Missing Keycloak claim: email",
                exception.getMessage()
        );

        verifyNoInteractions(userRepository);
    }
}
