package de.hs_esslingen.provacasy_backend.holiday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

// Reiner Unit Test: kein Spring, keine Datenbank, läuft deshalb in Sekundenbruchteilen.
// Jeder Test hat drei Teile: vorbereiten (Entity bauen), ausführen (mappen), prüfen (assert).
class PublicHolidayMapperTest {

    // Mappers.getMapper erzeugt die von MapStruct generierte PublicHolidayMapperImpl direkt,
    // ohne Spring. Im echten Code bekommt der Service den Mapper dagegen von Spring.
    private final PublicHolidayMapper mapper = Mappers.getMapper(PublicHolidayMapper.class);

    @Test
    void dateAndNameShouldBeCopied() {
        // vorbereiten
        PublicHoliday holiday = new PublicHoliday(LocalDate.of(2026, 12, 25), "1. Weihnachtstag");

        // ausführen. var: Java leitet den Typ (dto.PublicHoliday) selbst ab,
        // so muss man den langen Paketnamen nicht schreiben.
        var dto = mapper.toDto(holiday);

        // prüfen: assertEquals(erwartet, tatsächlich)
        assertEquals(LocalDate.of(2026, 12, 25), dto.getDate());
        assertEquals("1. Weihnachtstag", dto.getName());
    }

    @Test
    void everyHolidayInListShouldBeMapped() {
        List<PublicHoliday> holidays = List.of(
                new PublicHoliday(LocalDate.of(2026, 12, 24), "Heiligabend"),
                new PublicHoliday(LocalDate.of(2026, 12, 31), "Silvester"));

        var dtos = mapper.toDtoList(holidays);

        assertEquals(2, dtos.size());
        assertEquals(LocalDate.of(2026, 12, 24), dtos.get(0).getDate());
        assertEquals("Silvester", dtos.get(1).getName());
    }

    // Prüft die Null Prüfung, die MapStruct selbst erzeugt. Sieht banal aus, zählt aber
    // für die Testabdeckung (JaCoCo, Sonar).
    @Test
    void nullShouldStayNull() {
        assertNull(mapper.toDto(null));
    }
}