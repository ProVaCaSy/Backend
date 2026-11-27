package de.hs_esslingen.provacasy_backend.holiday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

// Aufbau wie AbsenceRepositoryTest: echtes PostgreSQL aus Testcontainers, Schema von Flyway,
// jeder Test in einer eigenen Transaktion, die am Ende zurückgerollt wird.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PublicHolidayRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private PublicHolidayRepository holidayRepository;

    // Vor jedem Test ein paar Feiertage aus 2026 speichern, absichtlich nicht sortiert.
    @BeforeEach
    void setUp() {
        holidayRepository.saveAndFlush(new PublicHoliday(LocalDate.of(2026, 12, 25), "1. Weihnachtstag"));
        holidayRepository.saveAndFlush(new PublicHoliday(LocalDate.of(2026, 1, 1), "Neujahr"));
        holidayRepository.saveAndFlush(new PublicHoliday(LocalDate.of(2026, 12, 24), "Heiligabend"));
    }

    @Test
    void holidaysOfYearShouldBeFoundSortedByDate() {
        List<PublicHoliday> result = holidayRepository.findByHolidayDateBetweenOrderByHolidayDate(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(3, result.size());
        assertEquals("Neujahr", result.get(0).getName());
        assertEquals("1. Weihnachtstag", result.get(2).getName());
    }

    @Test
    void bothBoundariesShouldBeIncluded() {
        // "Between" schließt beide Grenzen ein: 24.12. und 25.12. sind genau die Grenzen
        List<PublicHoliday> result = holidayRepository.findByHolidayDateBetweenOrderByHolidayDate(
                LocalDate.of(2026, 12, 24), LocalDate.of(2026, 12, 25));

        assertEquals(2, result.size());
    }

    @Test
    void existsShouldTellIfYearIsAlreadyStored() {
        // So erkennt der Service später, ob er die Feiertags API für ein Jahr fragen muss
        assertTrue(holidayRepository.existsByHolidayDateBetween(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
        assertFalse(holidayRepository.existsByHolidayDateBetween(
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31)));
    }
}
