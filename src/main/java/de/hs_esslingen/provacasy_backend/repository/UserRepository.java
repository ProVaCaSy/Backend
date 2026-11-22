
package de.hs_esslingen.provacasy_backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.hs_esslingen.provacasy_backend.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
}
