package de.hs_esslingen.provacasy_backend.absence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import de.hs_esslingen.provacasy_backend.user.User;

// Aufbau wie PublicHolidayMapperTest: Unit Test ohne Spring und ohne Datenbank.
class AbsenceMapperTest {

    private final AbsenceMapper mapper = Mappers.getMapper(AbsenceMapper.class);

    // Zwei verschiedene User, damit man sieht, dass userId und createdFrom nicht vertauscht werden.
    private final User prof = new User(UUID.randomUUID(), "Max", "Mustermann", "max@example.com");
    private final User secretary = new User(UUID.randomUUID(), "Erika", "Musterfrau", "erika@example.com");

    @Test
    void allFieldsShouldBeMapped() {
        Absence absence = new Absence(prof, secretary,
                LocalDate.of(2026, 12, 21), LocalDate.of(2026, 12, 23), "Urlaub", AbsenceStatus.APPROVED);
        // Diese Felder setzt sonst die Datenbank beim Speichern
        // Sie haben absichtlich keinen Setter. ReflectionTestUtils schreibt sie trotzdem von
        // außen hinein. Das ist nur im Test in Ordnung, nie im echten Code.
        ReflectionTestUtils.setField(absence, "id", 42L);
        ReflectionTestUtils.setField(absence, "version", (short) 0);
        ReflectionTestUtils.setField(absence, "createdAt", Instant.parse("2026-11-27T10:15:30Z"));

        var dto = mapper.toDto(absence);

        assertEquals(42L, dto.getId());
        assertEquals(prof.getId(), dto.getUserId());
        assertEquals(secretary.getId(), dto.getCreatedFrom());
        assertNull(dto.getUpdatedFrom());
        assertEquals(LocalDate.of(2026, 12, 21), dto.getStartDate());
        assertEquals(LocalDate.of(2026, 12, 23), dto.getEndDate());
        assertEquals("Urlaub", dto.getTitle());
        // Das Enum der Entity wurde über den Namen in das Enum des DTO übersetzt.
        assertEquals(de.hs_esslingen.provacasy_backend.dto.AbsenceStatus.APPROVED, dto.getStatus());
        assertEquals(0, dto.getVersion());
        // Hier greift die default Methode toOffsetDateTime aus dem Mapper (Instant zu UTC).
        assertEquals(OffsetDateTime.parse("2026-11-27T10:15:30Z"), dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    void updatedFromAndUpdatedAtShouldBeMapped() {
        Absence absence = new Absence(prof, prof,
                LocalDate.of(2026, 12, 21), LocalDate.of(2026, 12, 23), null, AbsenceStatus.OPEN);
        // Wie beim Ändern durch das Sekretariat: setzt updatedFrom und updatedAt zusammen.
        absence.markUpdated(secretary);

        var dto = mapper.toDto(absence);

        assertEquals(secretary.getId(), dto.getUpdatedFrom());
        assertEquals(ZoneOffset.UTC, dto.getUpdatedAt().getOffset());
    }

    @Test
    void nullShouldStayNull() {
        assertNull(mapper.toDto(null));
    }
}