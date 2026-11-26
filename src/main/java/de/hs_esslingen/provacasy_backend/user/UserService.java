
package de.hs_esslingen.provacasy_backend.user;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User syncCurrentUser(Jwt jwt) {

        UUID id = UUID.fromString(jwt.getSubject());

        String firstName = requiredClaim(jwt, "given_name");
        String lastName = requiredClaim(jwt, "family_name");
        String email = requiredClaim(jwt, "email");

        User user = userRepository.findById(id)
                .orElseGet(() -> new User(
                        id, firstName, lastName, email
                ));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);

        return userRepository.save(user);
    }

    private String requiredClaim(Jwt jwt, String name) {
        String value = jwt.getClaimAsString(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing Keycloak claim: " + name
            );
        }

        return value;
    }
}
