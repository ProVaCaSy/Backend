package de.hs_esslingen.provacasy_backend.entitlement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import de.hs_esslingen.provacasy_backend.user.User;

// Aufbau wie PublicHolidayMapperTest: Unit Test ohne Spring und ohne Datenbank.
class VacationEntitlementMapperTest {

    private final VacationEntitlementMapper mapper = Mappers.getMapper(VacationEntitlementMapper.class);

    @Test
    void userIdYearAndDaysShouldBeCopied() {
        // vorbereiten: Der Konstruktor braucht einen User, weil er daraus den Schlüssel baut.
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Max", "Mustermann", "max@example.com");
        // (short): Zahlen wie 2026 sind in Java immer int, der Konstruktor will aber short.
        VacationEntitlement entitlement = new VacationEntitlement(user, (short) 2026, (short) 28);

        // ausführen
        var dto = mapper.toDto(entitlement);

        // prüfen: userId kommt über "id.userId", year über getYear(), days von Short zu Integer.
        assertEquals(userId, dto.getUserId());
        assertEquals(2026, dto.getYear());
        assertEquals(28, dto.getDays());
    }

    @Test
    void nullShouldStayNull() {
        assertNull(mapper.toDto(null));
    }
}
