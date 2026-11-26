package de.hs_esslingen.provacasy_backend.holiday;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Datenzugriff auf die Tabelle public_holiday (Zwischenspeicher der Feiertags-API).
 */
public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, LocalDate> {

    /** Alle Feiertage zwischen from und to (beide eingeschlossen), zum Beispiel für ein ganzes Jahr. */
    List<PublicHoliday> findByHolidayDateBetweenOrderByHolidayDate(LocalDate from, LocalDate to);

    /** Sind für diesen Zeitraum schon Feiertage gespeichert? Wenn nicht, holt der Service sie von der API. */
    boolean existsByHolidayDateBetween(LocalDate from, LocalDate to);
}
