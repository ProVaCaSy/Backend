package de.hs_esslingen.provacasy_backend.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import de.hs_esslingen.provacasy_backend.dto.Role;

// Aufbau wie PublicHolidayMapperTest: Unit Test ohne Spring und ohne Datenbank.
class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void userAndRoleShouldBeMapped() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Max", "Mustermann", "max@example.com");

        var dto = mapper.toDto(user, Role.PROFESSOR);

        assertEquals(id, dto.getId());
        assertEquals("Max", dto.getFirstName());
        assertEquals("Mustermann", dto.getLastName());
        assertEquals("max@example.com", dto.getEmail());
        assertEquals(Role.PROFESSOR, dto.getRole());
    }

    // Studierende oder Nutzer ohne passende Gruppe haben keine Rolle (null).
    // Das Frontend zeigt dann die Landing Page.
    @Test
    void userWithoutRoleShouldHaveNoRole() {
        User user = new User(UUID.randomUUID(), "Lisa", "Studentin", "lisa@example.com");

        var dto = mapper.toDto(user, null);

        assertNull(dto.getRole());
    }
}